import CoreLocation
import MapLibre
import Shared

enum MapLibreCamera {
    static func applyInitialCenter(
        _ center: IosMapCoordinate,
        to mapView: MLNMapView
    ) {
        mapView.setCenter(
            CLLocationCoordinate2D(latitude: center.latitude, longitude: center.longitude),
            zoomLevel: MapLibreDarkStyle.initialZoom,
            animated: false
        )
    }

    static func focus(
        _ request: IosMapFocusRequest,
        on mapView: MLNMapView
    ) {
        mapView.setCenter(
            CLLocationCoordinate2D(latitude: request.latitude, longitude: request.longitude),
            zoomLevel: MapLibreDarkStyle.focusZoom,
            animated: true
        )
    }

    @discardableResult
    static func apply(
        _ command: IosMapCameraCommand,
        currentLocation: IosCurrentLocation?,
        to mapView: MLNMapView
    ) -> Bool {
        switch command.kind {
        case .zoomby:
            guard command.delta.isFinite, command.delta != 0 else { return false }
            let zoom = min(
                mapView.maximumZoomLevel,
                max(mapView.minimumZoomLevel, mapView.zoomLevel + command.delta)
            )
            mapView.setZoomLevel(zoom, animated: true)
            return true
        case .movetocurrentlocation:
            guard let location = currentLocation else { return false }
            let requestedZoom = command.zoom?.doubleValue
            let zoom = requestedZoom?.isFinite == true ? requestedZoom! : mapView.zoomLevel
            mapView.setCenter(
                CLLocationCoordinate2D(latitude: location.latitude, longitude: location.longitude),
                zoomLevel: zoom,
                animated: true
            )
            return true
        default:
            return false
        }
    }
}
