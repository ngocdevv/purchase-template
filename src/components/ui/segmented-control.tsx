import { LayoutChangeEvent, StyleSheet, Text, View } from 'react-native';

import { useEffect, useMemo, useState } from 'react';

import { PressableScale } from 'pressto';
import Animated, {
  cancelAnimation,
  Easing,
  FadeInDown,
  useAnimatedStyle,
  useSharedValue,
  withTiming
} from 'react-native-reanimated';
import { AnimatedText } from './animated-text';


const TimingConfig = {
  duration: 1000,
  easing: Easing.bezier(0.4, 0.0, 0.2, 1),
};

const INTERNAL_PADDING = 5;

type SegmentedControlProps<T extends { name: string; }> = {
  data: readonly T[];
  onPress: (item: T) => void;
  selected: T;
  height: number;
};

function SegmentedControl<T extends { name: string; }>({
  data,
  onPress,
  selected,
  height,
}: SegmentedControlProps<T>) {
  const internalPadding = INTERNAL_PADDING;
  const [width, setWidth] = useState<number>();
  const [cellLayouts, setCellLayouts] = useState<
    Record<number, { width: number; x: number }>
  >({});

  const cellBackgroundWidth = width ? width / data.length : 0;
  const activeIndexes = useSharedValue<number[]>([]);
  const highlightLeft = useSharedValue(0);
  const highlightWidth = useSharedValue(0);

  const selectedCellIndex = useMemo(
    () => data.findIndex(item => item === selected),
    [data, selected],
  );

  const blurProgress = useSharedValue(0);

  const rCellMessageStyle = useAnimatedStyle(() => {
    return {
      left: highlightLeft.value,
      width: highlightWidth.value,
    };
  }, [highlightLeft, highlightWidth]);


  useEffect(() => {
    if (selectedCellIndex < 0) {
      return;
    }
    const layout = cellLayouts[selectedCellIndex];
    const fallbackPadding =
      data.length > 1
        ? internalPadding -
        2 * internalPadding * (selectedCellIndex / (data.length - 1))
        : 0;
    const fallbackLeft =
      (Number.isFinite(cellBackgroundWidth) ? cellBackgroundWidth : 0) *
      selectedCellIndex +
      fallbackPadding;
    const fallbackWidth =
      cellBackgroundWidth - internalPadding / (data.length || 1);

    const targetLeft = layout?.x ?? fallbackLeft ?? 0;
    const targetWidth = layout?.width ?? Math.max(fallbackWidth, 0);

    highlightLeft.value = withTiming(targetLeft, TimingConfig);
    highlightWidth.value = withTiming(targetWidth, TimingConfig);
  }, [
    cellBackgroundWidth,
    cellLayouts,
    data.length,
    highlightLeft,
    highlightWidth,
    internalPadding,
    selectedCellIndex,
  ]);

  const deeperFadeInDown = FadeInDown.springify()
    .damping(10)
    .mass(1)
    .stiffness(10)
    .energyThreshold(6e-8)
    .withInitialValues({
      transform: [{ translateY: 140 }]
    })

  return (
    <Animated.View
      entering={deeperFadeInDown}
      onLayout={(event) => {
        setWidth(event.nativeEvent.layout.width)
      }}
      style={[
        styles.backgroundContainer,
        styles.backgroundSurface,
        { height },
      ]}>
      {data.map((item, index) => {

        const rOpacity = useAnimatedStyle(() => {
          return {
            opacity: withTiming(selectedCellIndex === index ? 1 : .7, TimingConfig)
          }

        }, [selectedCellIndex, index])

        return (
          <View
            key={item.name}
            onLayout={(event: LayoutChangeEvent) => {
              const { width: cellWidth, x } = event.nativeEvent.layout;
              setCellLayouts(prev => {
                const prevLayout = prev[index];
                if (
                  prevLayout &&
                  prevLayout.width === cellWidth &&
                  prevLayout.x === x
                ) {
                  return prev;
                }
                return {
                  ...prev,
                  [index]: { width: cellWidth, x },
                };
              });
            }}
            style={styles.cellWrapper}>
            <PressableScale
              style={styles.labelContainer}
              onPress={() => {
                onPress(item);
                const prevIndex = data.findIndex(
                  dataItem => dataItem.name === selected.name,
                );
                if (prevIndex === index) {
                  return;
                }
                activeIndexes.value = [prevIndex, index];
                cancelAnimation(blurProgress);
                blurProgress.value = withTiming(1, TimingConfig, () => {
                  blurProgress.value = 0;
                  activeIndexes.value = [];
                });
              }}>
              <Animated.View style={[styles.labelRow, { height: height - 12 }]}>
                <AnimatedText style={[styles.difficultyLabel]}>
                  {item.name}
                </AnimatedText>
                {item.name === "Yearly" && (
                  <Animated.View style={[styles.saveBadge, rOpacity]}>
                    <Text style={styles.saveBadgeText}>Save 25%</Text>
                  </Animated.View>
                )}
              </Animated.View>
            </PressableScale>
          </View>
        );
      })}

      <Animated.View
        style={[
          {
            height: height - internalPadding * 2,
          },
          styles.highlightedCellContent,
          rCellMessageStyle,
        ]}
      />
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  backgroundContainer: {
    borderRadius: 10,
    flexDirection: 'row',
    shadowOffset: { height: 1, width: 0 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  backgroundSurface: {
    alignSelf: 'center',
    backgroundColor: '#FFFFFF',
    marginVertical: 36,
    padding: INTERNAL_PADDING,
  },
  difficultyLabel: {
    fontSize: 12,
    textAlign: 'center',
    fontWeight: "900"
  },
  fill: {
    flex: 1,
  },
  highlightedCellContent: {
    alignSelf: 'center',
    backgroundColor: "#F5F5F5",
    borderCurve: 'continuous',
    borderRadius: 6,
    position: 'absolute',
    shadowOffset: { height: 1, width: 0 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    zIndex: 1,
  },
  labelContainer: {
    flexDirection: 'row',
    gap: 6,
    justifyContent: 'center',
    zIndex: 2,
    paddingHorizontal: 12
  },
  cellWrapper: {
    zIndex: 2,
  },
  labelRow: {
    alignItems: 'center',
    columnGap: 8,
    flexDirection: 'row',
  },
  saveBadge: {
    backgroundColor: '#FFFFFF',
    borderRadius: 30,
    padding: 3,
  },
  saveBadgeText: {
    fontSize: 11,
    fontWeight: '500',
  },
});

export { SegmentedControl };
