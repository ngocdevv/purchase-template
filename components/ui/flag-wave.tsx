// Fabric blowing effect shader
import {
  Canvas,
  Fill,
  Shader,
  Skia,
  useClock
} from "@shopify/react-native-skia";
import React from "react";
import { useWindowDimensions } from "react-native";
import Animated, {
  FadeIn,
  FadeOut,
  useDerivedValue
} from "react-native-reanimated";

const source = Skia.RuntimeEffect.Make(`
uniform vec3 uResolution;
uniform float uTime;
uniform vec3 uColor;
uniform float uAmplitude;
uniform float uSpeed;

vec4 main(vec2 fragCoord) {
  vec2 uv = fragCoord / uResolution.xy;
  
  float time = uTime * uSpeed;
  
  float wave1 = sin(uv.y * 10.0 + time * 2.0) * cos(uv.x * 8.0 - time * 1.5);
  float wave2 = sin(uv.y * 15.0 + time * 1.5) * sin(uv.x * 12.0 - time * 2.0);
  float wave3 = cos(uv.y * 8.0 + time * 1.0) * sin(uv.x * 6.0 - time * 1.8);
  
  float displacement = (wave1 * 0.4 + wave2 * 0.35 + wave3 * 0.25) * uAmplitude;
  
  float gradient = uv.x * 0.1 + 1;
  
  float wrinkle = sin(uv.y * 30.0 + displacement * 10.0) * 
                  sin(uv.x * 25.0 - time * 2.0 + displacement * 8.0) * 0.15;
  
  float diffuse = gradient + displacement * 0.6;
  diffuse = smoothstep(0.3, 1.0, diffuse);
  
  float specular = pow(max(displacement + wrinkle, 0.0), 3.0) * 0.3;
  
  float ao = 1.0 - (abs(displacement) * 0.4);
  ao = clamp(ao, 0.6, 1.0);
  
  float lighting = (diffuse * 0.8 + specular + wrinkle) * ao;
  lighting = clamp(lighting, 0.4, 1.15);
  
  vec3 shadowTint = mix(vec3(0.9), uColor, 0.8);
  vec3 col = mix(shadowTint, uColor, lighting);
  col = col * (0.8 + lighting * 0.3);
  
  col += vec3(0.06) * sin(displacement * 5.0);
  
  return vec4(col, 1.0);
}
`);

export default function FlagWave({
  color = [0.9, 0.9, 1.0],
  speed = 1.0,
  amplitude = 0.5,
  ...props
}: {
  color?: [number, number, number];
  speed?: number;
  amplitude?: number;
  width?: number;
  height?: number;
}) {
  const clock = useClock();

  const { width: screenWidth, height: screenHeight } = useWindowDimensions();
  const width = props.width ?? screenWidth;
  const height = props.height ?? screenHeight;


  const uniforms = useDerivedValue(() => {
    return {
      uResolution: [width, height, width / height],
      uTime: clock.value / 1000,
      uColor: color,
      uAmplitude: amplitude,
      uSpeed: speed,
    };
  }, [clock, width, height, color, amplitude, speed]);

  return (
    <Animated.View style={[{ height: 240 }]} entering={FadeIn.springify()
      .damping(10)
      .mass(3)
      .stiffness(10)} exiting={FadeOut}>
      <Canvas style={{ flex: 1 }}>
        <Fill>
          <Shader source={source} uniforms={uniforms} />
        </Fill>
      </Canvas>
    </Animated.View>
  );
}

FlagWave.displayName = "FlagWave";
