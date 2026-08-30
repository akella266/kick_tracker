import SwiftUI
import shared

@main
struct KickTrackerApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeRootView()
                .ignoresSafeArea(.keyboard)
        }
    }
}

struct ComposeRootView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
