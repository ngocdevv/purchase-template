import SwiftUI

struct PlanPicker: View {
    let selectedPlan: SubscriptionPlan
    let entered: Bool
    let select: (SubscriptionPlan) -> Void
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @ScaledMetric(relativeTo: .caption) private var fontSize: CGFloat = 12
    @ScaledMetric(relativeTo: .caption) private var badgeSize: CGFloat = 11
    @ScaledMetric(relativeTo: .caption) private var controlHeight: CGFloat = 38
    @Namespace private var highlight

    var body: some View {
        HStack(spacing: 0) {
            ForEach(SubscriptionPlan.allCases) { plan in
                Button { select(plan) } label: {
                    HStack(spacing: 8) {
                        Text(plan.rawValue).font(.system(size: fontSize, weight: .black))
                        if plan == .yearly {
                            Text("Save 25%")
                                .font(.system(size: badgeSize, weight: .medium))
                                .padding(3)
                                .background(.white, in: Capsule())
                                .opacity(selectedPlan == .yearly ? 1 : 0.7)
                        }
                    }
                    .foregroundStyle(.black)
                    .padding(.horizontal, 12)
                    .frame(height: controlHeight - 10)
                    .background {
                        if selectedPlan == plan {
                            RoundedRectangle(cornerRadius: 6, style: .continuous)
                                .fill(Color(white: 245 / 255))
                                .shadow(color: .black.opacity(0.1), radius: 2, y: 1)
                                .matchedGeometryEffect(id: "selection", in: highlight)
                        }
                    }
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(plan == .yearly ? "Yearly, Save 25%" : "Monthly")
                .accessibilityValue(selectedPlan == plan ? "Selected" : "")
                .accessibilityAddTraits(selectedPlan == plan ? .isSelected : [])
                .accessibilityIdentifier("purchase.plan.\(plan.rawValue.lowercased())")
            }
        }
        .padding(5)
        .frame(height: controlHeight)
        .background(.white, in: RoundedRectangle(cornerRadius: 10))
        .shadow(color: .black.opacity(0.1), radius: 2, y: 1)
        .animation(reduceMotion ? .easeOut(duration: 0.15) : PurchaseStyle.selection, value: selectedPlan)
        .padding(.vertical, 36)
        .modifier(Entrance(entered: entered, translation: 140))
    }
}
