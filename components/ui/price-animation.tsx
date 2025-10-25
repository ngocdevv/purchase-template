import React, { ForwardedRef, useImperativeHandle } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import type { SharedValue } from 'react-native-reanimated';
import Animated, {
  interpolate,
  useAnimatedStyle,
  useDerivedValue,
  useSharedValue,
  withDelay,
  withSpring,
} from 'react-native-reanimated';
import { AnimatedText } from './animated-text';

const FONT_SIZE = 56;
const FONT_HEIGHT = FONT_SIZE + 5;
const CHARACTER_DELAY = 40;
const SPRING_CONFIG = {
  duration: 350,
  dampingRatio: 2.8,
};

export type PriceAnimationRef = {
  animate: () => void;
  reset: () => void;
  toggle: () => void;
};

type PriceAnimationProps = {
  content: string;
};

type DigitProps = {
  digit: string;
  progress: SharedValue<number>;
};

const Digit: React.FC<DigitProps> = ({ digit, progress }) => {
  const topDigitStyle = useAnimatedStyle(() => {
    const rotateX = `${progress.value * 90}deg`;
    return {
      opacity: 1 - progress.value,
      transform: [
        { perspective: 1000 },
        { translateY: (-progress.value * FONT_HEIGHT) / 2 },
        { rotateX },
      ],
    };
  });

  const bottomDigitStyle = useAnimatedStyle(() => {
    const rotateX = interpolate(progress.value, [0, 1], [-90, 0]);
    const translateY = interpolate(progress.value, [0, 1], [FONT_HEIGHT / 2, 0]);
    return {
      opacity: progress.value,
      transform: [
        { translateY },
        { rotateX: `${rotateX}deg` },
      ],
    };
  });

  return (
    <Animated.View style={styles.digitContainer}>
      <AnimatedText style={[styles.digit, topDigitStyle]}>{digit}</AnimatedText>
      <AnimatedText
        style={[styles.digit, styles.absoluteDigit, bottomDigitStyle]}>
        {digit}
      </AnimatedText>
    </Animated.View>
  );
};

export const PriceAnimation = React.forwardRef(
  ({ content }: PriceAnimationProps, ref: ForwardedRef<PriceAnimationRef>) => {
    const progress = useSharedValue(0);

    const triggerAnimation = () => {
      progress.value = 0;
      requestAnimationFrame(() => {
        progress.value = 1;
      });
    };

    useImperativeHandle(ref, () => ({
      animate: triggerAnimation,
      reset: () => {
        progress.value = 0;
      },
      toggle: () => {
        progress.value = progress.value === 0 ? 1 : 0;
      },
    }));

    return (
      <View style={styles.row}>
        <Text style={styles.currency}>$</Text>
        {content.split('').map((char, index) => {
          // eslint-disable-next-line react-hooks/rules-of-hooks
          const delayedProgress = useDerivedValue(() => {
            'worklet';

            const delayMs = index * CHARACTER_DELAY;
            return withDelay(delayMs, withSpring(progress.value, SPRING_CONFIG));
          }, []);

          return (
            <Digit key={`${char}-${index}`} digit={char} progress={delayedProgress} />
          );
        })}
      </View>
    );
  },
);

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  currency: {
    color: '#FFFFFF',
    fontSize: 24,
    fontWeight: '600',
    marginRight: 4,
    marginTop: 10
  },
  digitContainer: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  digit: {
    color: '#FFFFFF',
    fontSize: FONT_SIZE,
    fontWeight: '700',
  },
  absoluteDigit: {
    position: 'absolute',
  },
});

PriceAnimation.displayName = 'PriceAnimation';
