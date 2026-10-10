import SwiftUI

struct FlippingPrice: View {
    let price: String
    let trigger: Int
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var progress: CGFloat = 0

    var body: some View {
        HStack(alignment: .top, spacing: 0) {
            Text("$").font(.system(size: 24, weight: .semibold))
                .padding(.top, 10).padding(.trailing, 4)
            ForEach(Array(price.enumerated()), id: \.offset) { index, character in
                Text(String(character))
                    .font(.system(size: 56, weight: .bold))
                    .modifier(FlippingDigit(progress: reduceMotion ? 1 : progress))
                    .animation(reduceMotion ? nil :
                        .spring(response: 0.35, dampingFraction: 2.8).delay(Double(index) * 0.04),
                        value: progress)
            }
        }
        .foregroundStyle(.white)
        .frame(height: 67, alignment: .topLeading)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("$\(price)")
        .onChange(of: trigger) { _, _ in progress = progress == 0 ? 1 : 0 }
    }
}

/// Both faces carry the current digit, as in the original animation.
private struct FlippingDigit: AnimatableModifier {
    var progress: CGFloat
    var animatableData: CGFloat {
        get { progress }
        set { progress = newValue }
    }

    func body(content: Content) -> some View {
        content.opacity(1 - progress)
            .rotation3DEffect(.degrees(Double(progress * 90)), axis: (1, 0, 0), perspective: 0.001)
            .offset(y: -progress * 61 / 2)
            .overlay {
                content.opacity(progress)
                    .rotation3DEffect(.degrees(Double((progress - 1) * 90)), axis: (1, 0, 0), perspective: 0.001)
                    .offset(y: (1 - progress) * 61 / 2)
            }
    }
}
