import SwiftUI

struct PurchaseHeader: View {
    let entered: Bool
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @ScaledMetric(relativeTo: .title) private var titleSize: CGFloat = 28
    @ScaledMetric(relativeTo: .caption) private var subtitleSize: CGFloat = 12

    var body: some View {
        VStack(spacing: 8) {
            (Text("One price.").foregroundColor(.black) +
                Text("One plan").foregroundColor(PurchaseStyle.highlight))
                .font(.system(size: titleSize, weight: .bold))
                .lineLimit(dynamicTypeSize.isAccessibilitySize ? nil : 1)
                .minimumScaleFactor(0.75)
                .frame(minHeight: 33)
                .modifier(Entrance(entered: entered))
                .accessibilityAddTraits(.isHeader)
                .accessibilityLabel("One price. One plan")
            Text("Get access to well-researched school articles, expert opinions, and inspiring student stories that keep you learning beyond the classroom.")
                .font(.system(size: subtitleSize))
                // FadeInDown in the video resolves to opacity 1, overriding the RN style's .6.
                .foregroundStyle(.black)
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)
                .frame(minHeight: 42)
                .modifier(Entrance(entered: entered, translation: 120))
        }
        .padding(.horizontal, 40)
        .padding(.top, 16)
    }
}
