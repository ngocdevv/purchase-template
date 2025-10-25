import { ButtonScale } from '@/components/ui/button-scale';
import { FabricColors } from '@/components/ui/fabric-colors';
import { Header } from '@/components/ui/header';
import { ListDescription } from '@/components/ui/list-desc';
import { PriceAnimation, PriceAnimationRef } from '@/components/ui/price-animation';
import { SegmentedControl } from '@/components/ui/segmented-control';
import { BottomSheetBackdrop, BottomSheetBackdropProps, BottomSheetModal, BottomSheetModalProvider, BottomSheetView } from '@gorhom/bottom-sheet';
import React, { useCallback, useRef, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import 'react-native-reanimated';
import Animated, { FadeInDown, ZoomIn } from 'react-native-reanimated';
const FlagWave = React.lazy(() => import("../components/ui/flag-wave"));

const Data: {
  name: string;
  color: [number, number, number];
  price: string
}[] = [
    { name: 'Yearly', color: FabricColors.rose, price: "25" },
    { name: 'Monthly', color: FabricColors.darkBlue, price: "18" },
  ];

export default function RootLayout() {
  const [selected, setSelected] = useState(Data[0]);
  const everybodyStaggeredTextRef = useRef<PriceAnimationRef>(null);
  const bottomSheetModalRef = useRef<BottomSheetModal>(null);
  const handlePresentModalPress = useCallback(() => {
    bottomSheetModalRef.current?.present();
    setTimeout(() => {
      everybodyStaggeredTextRef.current?.animate()
    }, 600)
  }, []);

  const renderBottomSheetBackdrop = useCallback(
    (props: BottomSheetBackdropProps) => (
      <BottomSheetBackdrop disappearsOnIndex={-1} appearsOnIndex={0} {...props} />
    ),
    [],
  )

  const deeperZoomInDown = ZoomIn
    .springify()
    .damping(10)
    .mass(0.7)
    .stiffness(10)
    .energyThreshold(6e-8);



  const deeperFadeInDown = FadeInDown.springify()
    .damping(10)
    .mass(1)
    .stiffness(10)
    .energyThreshold(6e-8)
    .withInitialValues({
      transform: [{ translateY: 180 }]
    })


  return (
    <GestureHandlerRootView style={styles.fill}>
      <BottomSheetModalProvider>
        <View style={styles.fillCenter}>
          <ButtonScale label='Purchase' onPress={handlePresentModalPress} />
          <BottomSheetModal
            backdropComponent={renderBottomSheetBackdrop}
            handleIndicatorStyle={{
              backgroundColor: "#D3D3D3",
              marginTop: 4,
              width: "12%"
            }}
            backgroundStyle={{
              borderRadius: 42,
            }}
            ref={bottomSheetModalRef}>
            <BottomSheetView style={[styles.fill, { paddingBottom: 36 }]}>
              <Animated.View entering={FadeInDown.springify()
                .damping(10)
                .mass(1)
                .stiffness(10)
                .energyThreshold(6e-8)} style={styles.fill}>
                <Header />
                <SegmentedControl
                  data={Data}
                  onPress={item => {
                    setSelected(item);
                    everybodyStaggeredTextRef.current?.toggle();
                  }}
                  selected={selected}
                  height={38}
                />
                <Animated.View style={styles.subscriptionCard} entering={deeperFadeInDown}>
                  <Animated.View entering={deeperZoomInDown} style={[styles.subscriptionCard, {marginHorizontal: 32}]}>
                    <FlagWave color={selected.color} />
                    <View style={styles.priceInfo}>
                      <PriceAnimation content={selected.price} ref={everybodyStaggeredTextRef} />
                      <Text style={styles.priceInfoLabel}>All round secure payment</Text>
                    </View>
                  </Animated.View>
                </Animated.View>
              </Animated.View>
              <ListDescription />
              <Animated.View entering={deeperFadeInDown}>
                <ButtonScale />
              </Animated.View>
            </BottomSheetView>
          </BottomSheetModal>
        </View>
      </BottomSheetModalProvider>
    </GestureHandlerRootView>
  );
}


const styles = StyleSheet.create({
  fill: {
    flex: 1
  },
  fillCenter: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center"
  },
  subscriptionCard: {
    borderRadius: 28,
    overflow: "hidden"
  },
  priceInfo: {
    position: "absolute",
    bottom: 24,
    left: 24
  },
  priceInfoLabel: {
    color: "#FFFFFF",
    fontSize: 13
  }
})
