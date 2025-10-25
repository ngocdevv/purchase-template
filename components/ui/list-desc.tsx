import { MaterialIcons } from '@expo/vector-icons';
import { StyleSheet, Text, View } from 'react-native';
import Animated, { FadeInDown } from 'react-native-reanimated';

const BENEFITS = [
  'Unlimited access to articles',
  'Watch full length videos',
  'See all school content',
  'Access to our community',
  'No ads',
] as const;

export const ListDescription = () => {
  const deeperFadeInDown = FadeInDown.springify()
    .damping(10)
    .mass(1)
    .stiffness(10)
    .energyThreshold(6e-8)
    .withInitialValues({
      transform: [{ translateY: 160 }]
    })
  return (
    <Animated.View style={styles.container} entering={deeperFadeInDown}>
      {BENEFITS.map(benefit => (
        <View key={benefit} style={styles.row}>
          <MaterialIcons name="check" size={16} color="#18181b" />
          <Text style={styles.text}>{benefit}</Text>
        </View>
      ))}
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 40,
    paddingVertical: 28,
    gap: 18,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    columnGap: 12,
  },
  text: {
    fontSize: 14,
    color: '#18181b',
    fontWeight: '500',
  },
});
