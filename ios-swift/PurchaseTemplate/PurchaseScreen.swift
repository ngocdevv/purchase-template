import SwiftUI
import UIKit

struct PurchaseScreen: View {
    @State private var selectedPlan = SubscriptionPlan.yearly
    @State private var isPresented = false
    @State private var sheetHeight: CGFloat = 782
    @State private var windowTopInset: CGFloat = 0
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        GeometryReader { geometry in
            let topInset = max(geometry.safeAreaInsets.top, windowTopInset)
            ZStack(alignment: .bottom) {
                Color.white
                if !isPresented {
                    GlossyButton(title: "Purchase") { isPresented = true }
                        .accessibilityIdentifier("purchase.open")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .transition(.identity)
                }

                if isPresented {
                    Color.black.opacity(0.5)
                        .contentShape(Rectangle())
                        .onTapGesture { dismiss() }
                        .accessibilityHidden(true)
                        .transition(.opacity)

                    Button(action: dismiss) { Color.clear.contentShape(Rectangle()) }
                        .buttonStyle(.plain)
                        .frame(height: max(0, geometry.size.height - sheetHeight - topInset))
                        .accessibilityLabel("Dismiss purchase sheet")
                        .accessibilityIdentifier("purchase.backdrop")
                        .padding(.top, topInset)
                        .frame(maxHeight: .infinity, alignment: .top)

                    PurchaseSheet(selectedPlan: $selectedPlan,
                                  screenSize: geometry.size,
                                  maximumHeight: geometry.size.height - max(60, topInset + 12),
                                  heightDidChange: { sheetHeight = $0 },
                                  dismiss: dismiss)
                        .transition(reduceMotion ? .opacity : .move(edge: .bottom))
                        .zIndex(1)
                }
            }
            .animation(reduceMotion ? .easeOut(duration: 0.15) :
                .interpolatingSpring(mass: 1, stiffness: 260, damping: 28), value: isPresented)
        }
        .ignoresSafeArea()
        .background(WindowSafeAreaReader(topInset: $windowTopInset))
    }

    private func dismiss() { isPresented = false }
}

private struct PurchaseSheet: View {
    @Binding var selectedPlan: SubscriptionPlan
    let screenSize: CGSize
    let maximumHeight: CGFloat
    let heightDidChange: (CGFloat) -> Void
    let dismiss: () -> Void

    @State private var entered = false
    @State private var priceTrigger = 0
    @State private var bodyHeight: CGFloat = 662
    @State private var dragOffset: CGFloat = 0
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @ScaledMetric(relativeTo: .body) private var buttonHeight: CGFloat = 56

    private var sheetHeight: CGFloat { min(bodyHeight + 28 + buttonHeight + 36, maximumHeight) }
    private var scrollHeight: CGFloat { max(0, sheetHeight - 28 - buttonHeight - 36) }

    var body: some View {
        VStack(spacing: 0) {
            Button(action: dismiss) {
                Capsule().fill(Color(red: 211 / 255, green: 211 / 255, blue: 211 / 255))
                    .frame(width: screenSize.width * 0.12, height: 4)
                    .frame(maxWidth: .infinity)
                    .frame(height: 28)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Dismiss purchase sheet")
            .accessibilityHint("Tap or drag down to close")
            .accessibilityIdentifier("purchase.dismiss")
            .simultaneousGesture(dismissGesture)

            ScrollView {
                VStack(spacing: 0) {
                    VStack(spacing: 0) {
                        PurchaseHeader(entered: entered)
                        PlanPicker(selectedPlan: selectedPlan, entered: entered) { plan in
                            selectedPlan = plan
                            priceTrigger += 1
                        }
                        subscriptionCard
                    }
                    .modifier(Entrance(entered: entered))
                    Benefits(entered: entered)
                }
                .background(GeometryReader { geometry in
                    Color.clear.preference(key: SheetBodyHeight.self, value: geometry.size.height)
                })
            }
            .scrollIndicators(.hidden)
            .scrollDisabled(bodyHeight <= scrollHeight + 1)
            .frame(height: scrollHeight)
            .onPreferenceChange(SheetBodyHeight.self) { bodyHeight = $0 }

            // The source's CTA is a press animation demo. It does not start a payment.
            GlossyButton(title: "Start free trial", expands: true, action: {})
                .accessibilityIdentifier("purchase.trial")
                .padding(.horizontal, 32)
                .modifier(Entrance(entered: entered, translation: 180))
                .padding(.bottom, 36)
        }
        .frame(maxWidth: .infinity)
        .frame(height: sheetHeight, alignment: .top)
        .background(.white)
        .clipShape(UnevenRoundedRectangle(topLeadingRadius: 42, topTrailingRadius: 42))
        .offset(y: dragOffset)
        .accessibilityAction(.escape, dismiss)
        .onAppear { heightDidChange(sheetHeight) }
        .onChange(of: sheetHeight) { _, height in heightDidChange(height) }
        .task {
            // Separate the initial view insertion from the source's content entrance.
            await Task.yield()
            guard !Task.isCancelled else { return }
            entered = true
            try? await Task.sleep(for: .milliseconds(600))
            guard !Task.isCancelled else { return }
            priceTrigger += 1
        }
    }

    private var subscriptionCard: some View {
        ZStack(alignment: .bottomLeading) {
            FabricView(color: selectedPlan.fabricColor, screenSize: screenSize, entered: entered)
            VStack(alignment: .leading, spacing: 0) {
                FlippingPrice(price: selectedPlan.price, trigger: priceTrigger)
                Text("All round secure payment")
                    .font(.system(size: 13))
                    .foregroundStyle(.white)
            }
            .padding(24)
        }
        .frame(height: 240)
        .clipShape(RoundedRectangle(cornerRadius: 28, style: .continuous))
        .modifier(Entrance(entered: entered, translation: 0, initialScale: 0, mass: 0.7))
        .padding(.horizontal, 32)
        .modifier(Entrance(entered: entered, translation: 180))
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(selectedPlan.rawValue), $\(selectedPlan.price). All round secure payment")
        .accessibilityIdentifier("purchase.priceCard")
    }

    private var dismissGesture: some Gesture {
        DragGesture(minimumDistance: 8)
            .onChanged { dragOffset = max(0, $0.translation.height) }
            .onEnded { value in
                if value.translation.height > 80 || value.predictedEndTranslation.height > 160 {
                    dismiss()
                } else {
                    withAnimation(reduceMotion ? .easeOut(duration: 0.15) :
                        .interpolatingSpring(mass: 1, stiffness: 260, damping: 28)) {
                        dragOffset = 0
                    }
                }
            }
    }
}

private struct SheetBodyHeight: PreferenceKey {
    static var defaultValue: CGFloat = 662
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) { value = nextValue() }
}

/// Full-screen drawing ignores SwiftUI's safe area, but the backdrop's accessible
/// hit target must stay below the status bar. Read the hosting window's real inset.
private struct WindowSafeAreaReader: UIViewRepresentable {
    @Binding var topInset: CGFloat

    func makeUIView(context: Context) -> WindowInsetView {
        let view = WindowInsetView()
        view.isUserInteractionEnabled = false
        view.isAccessibilityElement = false
        updateUIView(view, context: context)
        return view
    }

    func updateUIView(_ uiView: WindowInsetView, context: Context) {
        uiView.report = { inset in
            DispatchQueue.main.async {
                if topInset != inset { topInset = inset }
            }
        }
        uiView.reportInset()
    }
}

private final class WindowInsetView: UIView {
    var report: ((CGFloat) -> Void)?

    override func didMoveToWindow() {
        super.didMoveToWindow()
        reportInset()
    }

    override func safeAreaInsetsDidChange() {
        super.safeAreaInsetsDidChange()
        reportInset()
    }

    func reportInset() {
        guard let window else { return }
        report?(window.safeAreaInsets.top)
    }
}

#Preview { PurchaseScreen() }
