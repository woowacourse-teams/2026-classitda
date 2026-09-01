import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    private static let mapFactory = IosMapFactory()
    private static let breathDetector = BreathAudioDetector()

    func makeUIViewController(context: Self.Context) -> UIViewController {
        IosMapBridge.shared.registerFactory(factory: Self.mapFactory)
        IosBreathBridge.shared.attach(
            onStart: { Self.breathDetector.start() },
            onStop: { Self.breathDetector.stop() }
        )
        return MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
