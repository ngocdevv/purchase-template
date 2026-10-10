import SwiftUI

struct GlossyButton: View {
    let title: String
    var expands = false
    let action: () -> Void

    @ScaledMetric(relativeTo: .body) private var labelSize: CGFloat = 16
    @ScaledMetric(relativeTo: .body) private var surfaceHeight: CGFloat = 48

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: labelSize, weight: .semibold))
                .foregroundStyle(Color(red: 59 / 255, green: 59 / 255, blue: 66 / 255))
                .padding(.horizontal, 48)
                .frame(maxWidth: expands ? .infinity : nil)
                .frame(height: surfaceHeight)
                .background {
                    Capsule().fill(Color(red: 249 / 255, green: 249 / 255, blue: 251 / 255))
                        .overlay(alignment: .top) {
                            Capsule().fill(.white.opacity(0.85))
                                .frame(height: 16).padding(.top, 4).padding(.horizontal, 10)
                        }
                        .clipShape(Capsule())
                }
                .padding(4)
                .contentShape(Capsule())
        }
        .buttonStyle(GlossyPressStyle())
    }
}

private struct GlossyPressStyle: ButtonStyle {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .background {
                // Shadow only the opaque rim. Shadowing the entire label also
                // shadows the text and shine separately in SwiftUI.
                Capsule()
                    .fill(configuration.isPressed
                        ? Color(red: 225 / 255, green: 225 / 255, blue: 231 / 255)
                        : Color(red: 231 / 255, green: 231 / 255, blue: 235 / 255))
                    .shadow(color: Color(red: 124 / 255, green: 124 / 255, blue: 127 / 255)
                        .opacity(configuration.isPressed ? 0.12 : 0.25),
                        radius: configuration.isPressed ? 6 : 12,
                        x: 0, y: configuration.isPressed ? 3 : 8)
            }
            .scaleEffect(configuration.isPressed && !reduceMotion ? 0.97 : 1)
            .animation(reduceMotion ? .easeOut(duration: 0.12) :
                .interpolatingSpring(mass: 1, stiffness: 260, damping: 20),
                value: configuration.isPressed)
    }
}
