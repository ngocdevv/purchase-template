import { BottomSheetModalProvider } from '@gorhom/bottom-sheet';
import { StyleSheet } from 'react-native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import 'react-native-reanimated';
import SubScreen from './src/screens/SubScreen';

export default function App() {
  return (
    <GestureHandlerRootView style={styles.fill}>
      <BottomSheetModalProvider>
        <SubScreen />
      </BottomSheetModalProvider>
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  fill: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor:"black"
  },
});
