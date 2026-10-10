#include <metal_stdlib>
using namespace metal;

// A direct translation of src/components/ui/flag-wave.tsx's SkSL.
[[ stitchable ]] half4 fabric(float2 position, half4 inputColor, float2 resolution,
                            float elapsed, float3 baseColor, float amplitude, float speed) {
    float2 uv = position / resolution;
    float time = elapsed * speed;
    float wave1 = sin(uv.y * 10.0 + time * 2.0) * cos(uv.x * 8.0 - time * 1.5);
    float wave2 = sin(uv.y * 15.0 + time * 1.5) * sin(uv.x * 12.0 - time * 2.0);
    float wave3 = cos(uv.y * 8.0 + time) * sin(uv.x * 6.0 - time * 1.8);
    float displacement = (wave1 * 0.4 + wave2 * 0.35 + wave3 * 0.25) * amplitude;
    float gradient = uv.x * 0.1 + 1;
    float wrinkle = sin(uv.y * 30.0 + displacement * 10.0)
        * sin(uv.x * 25.0 - time * 2.0 + displacement * 8.0) * 0.15;
    float diffuse = smoothstep(0.3, 1.0, gradient + displacement * 0.6);
    float specular = pow(max(displacement + wrinkle, 0.0), 3.0) * 0.3;
    float ao = clamp(1.0 - abs(displacement) * 0.4, 0.6, 1.0);
    float lighting = clamp((diffuse * 0.8 + specular + wrinkle) * ao, 0.4, 1.15);
    float3 shadowTint = mix(float3(0.9), baseColor, 0.8);
    float3 color = mix(shadowTint, baseColor, lighting) * (0.8 + lighting * 0.3);
    color += float3(0.06) * sin(displacement * 5.0);
    return half4(half3(color), inputColor.a);
}
