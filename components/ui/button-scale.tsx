import React from 'react';
import {
  Pressable,
  StyleProp,
  StyleSheet,
  Text,
  View,
  ViewStyle,
} from 'react-native';
import Animated, {
  useAnimatedStyle,
  useSharedValue,
  withSpring,
} from 'react-native-reanimated';

const SPRING_CONFIG = {
  damping: 20,
  mass: 1,
  stiffness: 260,
};

type ButtonScaleProps = {
  label?: string;
  onPress?: () => void;
  disabled?: boolean;
  style?: StyleProp<ViewStyle>;
};

export const ButtonScale: React.FC<ButtonScaleProps> = ({
  label = 'Start free trial',
  onPress,
  disabled = false,
  style,
}) => {
  const scale = useSharedValue(1);

  const animatedStyle = useAnimatedStyle(
    () => ({
      transform: [{ scale: scale.value }],
      opacity: disabled ? 0.5 : 1,
    }),
    [disabled],
  );

  const handlePressIn = () => {
    if (disabled) {
      return;
    }
    scale.value = withSpring(0.97, SPRING_CONFIG);
  };

  const handlePressOut = () => {
    scale.value = withSpring(1, SPRING_CONFIG);
  };

  return (
    <Animated.View style={[styles.wrapper, style, animatedStyle]}>
      <Pressable
        onPress={onPress}
        disabled={disabled}
        onPressIn={handlePressIn}
        onPressOut={handlePressOut}
        hitSlop={6}
        style={({ pressed }) => [
          styles.pressable,
          pressed && styles.pressablePressed,
        ]}>
        <View style={styles.surface}>
          <View pointerEvents="none" style={styles.highlight} />
          <Text style={styles.label}>{label}</Text>
        </View>
      </Pressable>
    </Animated.View>
  );
};

ButtonScale.displayName = 'ButtonScale';

const styles = StyleSheet.create({
  wrapper: {
    marginHorizontal: 32,
  },
  pressable: {
    borderRadius: 999,
    padding: 4,
    backgroundColor: '#E7E7EB',
    shadowColor: '#7c7c7f',
    shadowOpacity: 0.25,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 8 },
    elevation: 6,
  },
  pressablePressed: {
    shadowOpacity: 0.12,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 3 },
    backgroundColor: '#E1E1E7',
  },
  surface: {
    overflow: 'hidden',
    borderRadius: 999,
    paddingVertical: 14,
    paddingHorizontal: 48,
    backgroundColor: '#F9F9FB',
    alignItems: 'center',
    justifyContent: 'center',
  },
  highlight: {
    position: 'absolute',
    top: 4,
    left: 10,
    right: 10,
    height: 16,
    borderRadius: 999,
    backgroundColor: '#FFFFFF',
    opacity: 0.85,
  },
  lowlight: {
    position: 'absolute',
    bottom: 6,
    left: 18,
    right: 18,
    height: 18,
    borderRadius: 999,
    backgroundColor: '#D8D8DE',
    opacity: 0.35,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#3B3B42',
  },
});
