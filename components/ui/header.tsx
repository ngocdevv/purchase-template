import { StyleSheet, Text, View } from "react-native"
import Animated, { FadeInDown } from "react-native-reanimated"



export const Header = () => {

    const deeperFadeInDown = FadeInDown.springify()
        .damping(10)
        .mass(1)
        .stiffness(10)
        .energyThreshold(6e-8)
        .withInitialValues({
            transform: [{ translateY: 120 }]
        })
    return (
        <View style={styles.heroContent}>
             <Animated.Text entering={FadeInDown.springify()
                .damping(10)
                .mass(1)
                .stiffness(10)
                .energyThreshold(6e-8)} style={styles.heroTitle}>One price.<Text style={styles.heroTitleHighlight}>One plan</Text></Animated.Text>
            <Animated.Text entering={deeperFadeInDown} style={styles.heroSubtitle}>Get access to well-researched school articles, expert opinions, and inspiring student stories that keep you learning beyond the classroom.</Animated.Text>
        </View>
    )
}


const styles = StyleSheet.create({
    heroContent: {
        alignItems: "center",
        rowGap: 8,
        paddingHorizontal: 40,
        marginTop: 16
    },
    heroTitle: {
        fontSize: 28,
        fontWeight: "bold"
    },
    heroTitleHighlight: {
        color: "#C51A3C"
    },
    heroSubtitle: {
        textAlign: "center",
        opacity: 0.6,
        fontSize: 12
    },
})