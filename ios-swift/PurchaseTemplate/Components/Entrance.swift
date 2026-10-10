import SwiftUI

/// The same physical spring and distinct starting offsets as the Reanimated views.
struct Entrance: ViewModifier {
    let entered: Bool
    var translation: CGFloat = 25
    var initialScale: CGFloat = 1
    var mass: Double = 1

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    func body(content: Content) -> some View {
        content
            .opacity(entered ? 1 : 0)
            .offset(y: entered || reduceMotion ? 0 : translation)
            .scaleEffect(entered || reduceMotion ? 1 : initialScale)
            .animation(reduceMotion ? .easeOut(duration: 0.15) :
                .interpolatingSpring(mass: mass, stiffness: 10, damping: 10), value: entered)
    }
}
