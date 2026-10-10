import SwiftUI

struct FabricView: View {
    let color: SIMD3<Float>
    let screenSize: CGSize
    let entered: Bool

    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var start = Date()

    var body: some View {
        TimelineView(.animation(minimumInterval: 1 / 60, paused: reduceMotion || scenePhase != .active)) { timeline in
            Rectangle().fill(.white)
                .colorEffect(ShaderLibrary.fabric(
                    // Deliberately use the window size, NOT the size of this 240pt card.
                    .float2(Float(screenSize.width), Float(screenSize.height)),
                    .float(reduceMotion ? 0 : Float(timeline.date.timeIntervalSince(start))),
                    .float3(color.x, color.y, color.z), .float(0.5), .float(1)
                ))
        }
        .modifier(Entrance(entered: entered, translation: 0, mass: 3))
        .accessibilityHidden(true)
    }
}
