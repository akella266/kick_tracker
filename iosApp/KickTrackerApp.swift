import SwiftUI
import UIKit
import shared

@main
struct KickTrackerApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeRootView()
                .defersSystemGestures(on: [])
                .persistentSystemOverlays(.visible)
        }
    }
}

struct ComposeRootView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        SystemGestureHostingController(
            contentViewController: MainViewControllerKt.MainViewController()
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

final class SystemGestureHostingController: UIViewController {
    private let contentViewController: UIViewController

    init(contentViewController: UIViewController) {
        self.contentViewController = contentViewController
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        addChild(contentViewController)
        view.addSubview(contentViewController.view)
        contentViewController.view.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            contentViewController.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            contentViewController.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            contentViewController.view.topAnchor.constraint(equalTo: view.topAnchor),
            contentViewController.view.bottomAnchor.constraint(equalTo: view.bottomAnchor),
        ])
        contentViewController.didMove(toParent: self)
    }

    override var preferredScreenEdgesDeferringSystemGestures: UIRectEdge {
        []
    }

    override var childForScreenEdgesDeferringSystemGestures: UIViewController? {
        nil
    }

    override var prefersHomeIndicatorAutoHidden: Bool {
        false
    }

    override var childForHomeIndicatorAutoHidden: UIViewController? {
        nil
    }
}
