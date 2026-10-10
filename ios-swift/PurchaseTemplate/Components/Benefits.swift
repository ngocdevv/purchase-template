import SwiftUI

struct Benefits: View {
    let entered: Bool
    @ScaledMetric(relativeTo: .body) private var textSize: CGFloat = 14
    private let benefits = [
        "Unlimited access to articles", "Watch full length videos", "See all school content",
        "Access to our community", "No ads"
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            ForEach(benefits, id: \.self) { benefit in
                HStack(alignment: .center, spacing: 12) {
                    Image(systemName: "checkmark")
                        .font(.system(size: 14, weight: .regular))
                        .frame(width: 16)
                        .accessibilityHidden(true)
                    Text(benefit)
                        .font(.system(size: textSize, weight: .medium))
                        .fixedSize(horizontal: false, vertical: true)
                        .frame(minHeight: 17)
                }
                .foregroundStyle(PurchaseStyle.ink)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 40)
        .padding(.vertical, 28)
        .modifier(Entrance(entered: entered, translation: 160))
    }
}
