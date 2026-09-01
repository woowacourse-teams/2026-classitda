import CoreLocation
import MapLibre
import Shared
import UIKit
import QuartzCore

final class MapLibreRenderer: NSObject, MLNMapViewDelegate, UIGestureRecognizerDelegate {
    let mapView: MLNMapView

    private let eventSink: IosMapEventSink
    private var pendingState: IosMapRenderState?
    private var sighSource: MLNShapeSource?
    private var sighLayer: MLNSymbolStyleLayer?
    private var sighPulseDisplayLink: CADisplayLink?
    private var sighPulseStartedAt = CACurrentMediaTime()
    private var currentLocationSource: MLNShapeSource?
    private var styleIsReady = false
    private var didApplyProvisionalCamera = false
    private var didResolveInitialCamera = false
    private var lastCameraCommandID: Int64?
    private var lastReceivedCameraCommandID: Int64?
    private var lastFocusRequestID: String?
    private var pendingCameraCommands: [IosMapCameraCommand] = []

    private static let userCameraReasonMask: UInt =
        (1 << 1) | (1 << 2) | (1 << 3) | (1 << 4) |
        (1 << 5) | (1 << 6) | (1 << 7) | (1 << 8)

    init(eventSink: IosMapEventSink) {
        self.eventSink = eventSink
        mapView = MLNMapView(frame: .zero, styleURL: MapLibreDarkStyle.styleURL)
        super.init()

        mapView.backgroundColor = MapLibreDarkStyle.mapBackground
        mapView.delegate = self
        mapView.showsUserLocation = false
        mapView.allowsScrolling = true
        mapView.allowsZooming = true
        mapView.minimumZoomLevel = MapLibreDarkStyle.minimumZoom
        mapView.maximumZoomLevel = MapLibreDarkStyle.maximumZoom

        let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(handleMapTap(_:)))
        tapRecognizer.cancelsTouchesInView = false
        tapRecognizer.delegate = self
        mapView.addGestureRecognizer(tapRecognizer)
    }

    func update(state: IosMapRenderState) {
        dispatchPrecondition(condition: .onQueue(.main))
        pendingState = state
        if let command = state.cameraCommand, command.id != lastReceivedCameraCommandID {
            lastReceivedCameraCommandID = command.id
            pendingCameraCommands.append(command)
        }
        guard styleIsReady else { return }

        updateSighs(state.sighMarkers)
        updateCurrentLocation(state.currentLocation)
        applyCameraState(state)
    }

    func releaseResources() {
        mapView.delegate = nil
        pendingState = nil
        sighSource = nil
        sighLayer = nil
        sighPulseDisplayLink?.invalidate()
        sighPulseDisplayLink = nil
        currentLocationSource = nil
        pendingCameraCommands.removeAll()
    }

    func mapView(_ mapView: MLNMapView, didFinishLoading style: MLNStyle) {
        styleIsReady = false
        MapLibreDarkStyle.addKoreanPoiLayerIfPossible(to: style)
        addRuntimeSourcesAndLayers(to: style)
        styleIsReady = true

        if let state = pendingState {
            update(state: state)
        }
    }

    func mapViewDidFailLoadingMap(_ mapView: MLNMapView, withError error: Error) {
        eventSink.onStyleLoadFailed()
    }

    func mapViewRendererDidError(_ mapView: MLNMapView) {
        eventSink.onStyleLoadFailed()
    }

    func mapView(
        _ mapView: MLNMapView,
        regionWillChangeWith reason: MLNCameraChangeReason,
        animated: Bool
    ) {
        if reason.rawValue & Self.userCameraReasonMask != 0 {
            didResolveInitialCamera = true
        }
    }

    func gestureRecognizer(
        _ gestureRecognizer: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer
    ) -> Bool {
        true
    }

    @objc
    private func handleMapTap(_ recognizer: UITapGestureRecognizer) {
        guard recognizer.state == .ended else { return }
        let point = recognizer.location(in: mapView)
        let hitRect = CGRect(x: point.x - 22, y: point.y - 22, width: 44, height: 44)
        let features = mapView.visibleFeatures(
            in: hitRect,
            styleLayerIdentifiers: [MapLibreDarkStyle.sighLayerID],
            predicate: nil
        )
        guard let feature = features.first else { return }

        if let id = feature.attribute(forKey: "id") as? String {
            eventSink.onSighClick(id: id)
        } else if let id = feature.identifier as? String {
            eventSink.onSighClick(id: id)
        }
    }

    private func addRuntimeSourcesAndLayers(to style: MLNStyle) {
        style.setImage(MapLibreDarkStyle.makeSighStarImage(), forName: MapLibreDarkStyle.sighImageID)

        let sighSource = MLNShapeSource(
            identifier: MapLibreDarkStyle.sighSourceID,
            features: [],
            options: [.clustered: false]
        )
        style.addSource(sighSource)
        self.sighSource = sighSource

        let sighLayer = MLNSymbolStyleLayer(identifier: MapLibreDarkStyle.sighLayerID, source: sighSource)
        sighLayer.iconImageName = NSExpression(forConstantValue: MapLibreDarkStyle.sighImageID)
        sighLayer.iconScale = NSExpression(forConstantValue: 0.3)
        sighLayer.iconAllowsOverlap = NSExpression(forConstantValue: true)
        sighLayer.iconIgnoresPlacement = NSExpression(forConstantValue: true)
        style.addLayer(sighLayer)
        self.sighLayer = sighLayer
        startSighPulse()

        let currentSource = MLNShapeSource(
            identifier: MapLibreDarkStyle.currentLocationSourceID,
            features: [],
            options: nil
        )
        style.addSource(currentSource)
        currentLocationSource = currentSource

        let accuracyLayer = MLNFillStyleLayer(
            identifier: MapLibreDarkStyle.currentLocationAccuracyLayerID,
            source: currentSource
        )
        accuracyLayer.predicate = NSPredicate(format: "kind == 'accuracy'")
        accuracyLayer.fillColor = NSExpression(forConstantValue: MapLibreDarkStyle.locationBlue)
        accuracyLayer.fillOpacity = NSExpression(forConstantValue: 0.14)
        style.addLayer(accuracyLayer)

        let borderLayer = MLNCircleStyleLayer(
            identifier: MapLibreDarkStyle.currentLocationBorderLayerID,
            source: currentSource
        )
        borderLayer.predicate = NSPredicate(format: "kind == 'point'")
        borderLayer.circleColor = NSExpression(forConstantValue: UIColor.white)
        borderLayer.circleRadius = NSExpression(forConstantValue: 9)
        style.addLayer(borderLayer)

        let centerLayer = MLNCircleStyleLayer(
            identifier: MapLibreDarkStyle.currentLocationLayerID,
            source: currentSource
        )
        centerLayer.predicate = NSPredicate(format: "kind == 'point'")
        centerLayer.circleColor = NSExpression(forConstantValue: MapLibreDarkStyle.locationBlue)
        centerLayer.circleRadius = NSExpression(forConstantValue: 6)
        style.addLayer(centerLayer)
    }

    private func updateSighs(_ markers: [IosSighMarker]) {
        let features = markers.map { marker -> MLNPointFeature in
            let feature = MLNPointFeature()
            feature.coordinate = CLLocationCoordinate2D(latitude: marker.latitude, longitude: marker.longitude)
            feature.identifier = marker.id as NSString
            feature.attributes = ["id": marker.id]
            return feature
        }
        sighSource?.shape = MLNShapeCollectionFeature(shapes: features)
    }

    private func startSighPulse() {
        sighPulseDisplayLink?.invalidate()
        sighPulseStartedAt = CACurrentMediaTime()
        let displayLink = CADisplayLink(target: self, selector: #selector(updateSighPulse))
        displayLink.preferredFramesPerSecond = 30
        displayLink.add(to: .main, forMode: .common)
        sighPulseDisplayLink = displayLink
    }

    @objc
    private func updateSighPulse() {
        guard let sighLayer else { return }
        let elapsed = CACurrentMediaTime() - sighPulseStartedAt
        let wave = (sin(elapsed * 2 * .pi / 1.8) + 1) / 2
        let pulse = wave * wave * (3 - (2 * wave))
        sighLayer.iconScale = NSExpression(forConstantValue: 0.24 + (pulse * 0.12))
        sighLayer.iconOpacity = NSExpression(forConstantValue: 0.72 + (pulse * 0.28))
    }

    private func updateCurrentLocation(_ location: IosCurrentLocation?) {
        guard let location else {
            currentLocationSource?.shape = MLNShapeCollectionFeature(shapes: [])
            return
        }

        let center = MLNPointFeature()
        center.coordinate = CLLocationCoordinate2D(latitude: location.latitude, longitude: location.longitude)
        center.attributes = ["kind": "point"]

        var shapes: [MLNShape & MLNFeature] = [center]
        if location.accuracyMeters > 0 {
            var coordinates = accuracyRing(
                latitude: location.latitude,
                longitude: location.longitude,
                radiusMeters: location.accuracyMeters
            )
            let accuracy = MLNPolygonFeature(coordinates: &coordinates, count: UInt(coordinates.count))
            accuracy.attributes = ["kind": "accuracy"]
            shapes.insert(accuracy, at: 0)
        }
        currentLocationSource?.shape = MLNShapeCollectionFeature(shapes: shapes)
    }

    private func applyCameraState(_ state: IosMapRenderState) {
        if !didResolveInitialCamera, let center = state.initialCenter {
            if state.initialCenterIsProvisional {
                if !didApplyProvisionalCamera {
                    MapLibreCamera.applyInitialCenter(center, to: mapView)
                    didApplyProvisionalCamera = true
                }
            } else {
                MapLibreCamera.applyInitialCenter(center, to: mapView)
                didResolveInitialCamera = true
            }
        }

        if let focus = state.focusRequest, focus.id != lastFocusRequestID {
            lastFocusRequestID = focus.id
            MapLibreCamera.focus(focus, on: mapView)
            didResolveInitialCamera = true
        }

        while !pendingCameraCommands.isEmpty {
            let command = pendingCameraCommands.removeFirst()
            guard command.id != lastCameraCommandID else { continue }
            lastCameraCommandID = command.id
            if MapLibreCamera.apply(command, currentLocation: state.currentLocation, to: mapView) {
                didResolveInitialCamera = true
            }
        }
    }

    private func accuracyRing(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double
    ) -> [CLLocationCoordinate2D] {
        let earthRadiusMeters = 6_371_008.8
        let angularDistance = radiusMeters / earthRadiusMeters
        let latitudeRadians = latitude * .pi / 180
        let longitudeRadians = longitude * .pi / 180

        return (0...64).map { index in
            let bearing = Double(index) * 2 * .pi / 64
            let targetLatitude = asin(
                sin(latitudeRadians) * cos(angularDistance)
                    + cos(latitudeRadians) * sin(angularDistance) * cos(bearing)
            )
            let targetLongitude = longitudeRadians + atan2(
                sin(bearing) * sin(angularDistance) * cos(latitudeRadians),
                cos(angularDistance) - sin(latitudeRadians) * sin(targetLatitude)
            )
            return CLLocationCoordinate2D(
                latitude: targetLatitude * 180 / .pi,
                longitude: targetLongitude * 180 / .pi
            )
        }
    }
}
