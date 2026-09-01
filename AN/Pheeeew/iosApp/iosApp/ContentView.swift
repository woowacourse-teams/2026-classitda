import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    private static let mapFactory = IosMapFactory()

    func makeUIViewController(context: Self.Context) -> UIViewController {
        IosMapBridge.shared.registerFactory(factory: Self.mapFactory)
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
