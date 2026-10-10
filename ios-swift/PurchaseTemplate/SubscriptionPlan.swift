import SwiftUI

enum SubscriptionPlan: String, CaseIterable, Identifiable {
    case yearly = "Yearly"
    case monthly = "Monthly"

    var id: Self { self }
    var price: String { self == .yearly ? "25" : "18" }
    var fabricColor: SIMD3<Float> {
        self == .yearly ? SIMD3(0.9, 0.6, 0.7) : SIMD3(0.1, 0.2, 0.4)
    }
}

enum PurchaseStyle {
    static let ink = Color(red: 24 / 255, green: 24 / 255, blue: 27 / 255)
    static let highlight = Color(red: 197 / 255, green: 26 / 255, blue: 60 / 255)
    static let entrance = Animation.interpolatingSpring(
        mass: 1, stiffness: 10, damping: 10, initialVelocity: 0
    )
    static let selection = Animation.timingCurve(0.4, 0, 0.2, 1, duration: 1)
}
