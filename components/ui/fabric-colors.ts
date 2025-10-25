// Color presets for fabric blowing effects
// Colors are in RGB format [R, G, B] with values from 0.0 to 1.0

export const FabricColors = {
  // Blues
  darkBlue: [0.1, 0.2, 0.4] as [number, number, number],
  oceanBlue: [0.2, 0.4, 0.6] as [number, number, number],
  skyBlue: [0.4, 0.6, 0.9] as [number, number, number],
  lightBlue: [0.7, 0.85, 1.0] as [number, number, number],
  
  // Purples
  deepPurple: [0.3, 0.1, 0.5] as [number, number, number],
  lavender: [0.7, 0.6, 0.9] as [number, number, number],
  violet: [0.5, 0.3, 0.7] as [number, number, number],
  
  // Greens
  emerald: [0.1, 0.5, 0.4] as [number, number, number],
  mint: [0.6, 0.9, 0.8] as [number, number, number],
  forestGreen: [0.2, 0.4, 0.2] as [number, number, number],
  
  // Reds/Pinks
  crimson: [0.6, 0.1, 0.2] as [number, number, number],
  coral: [1.0, 0.5, 0.4] as [number, number, number],
  rose: [0.9, 0.6, 0.7] as [number, number, number],
  
  // Neutrals
  white: [0.9, 0.9, 1.0] as [number, number, number],
  cream: [1.0, 0.98, 0.9] as [number, number, number],
  silver: [0.75, 0.75, 0.8] as [number, number, number],
  charcoal: [0.2, 0.2, 0.25] as [number, number, number],
  black: [0.1, 0.1, 0.1] as [number, number, number],
  
  // Warm tones
  gold: [1.0, 0.84, 0.3] as [number, number, number],
  amber: [1.0, 0.75, 0.3] as [number, number, number],
  bronze: [0.8, 0.5, 0.2] as [number, number, number],
  
  // Cool tones
  teal: [0.2, 0.6, 0.6] as [number, number, number],
  aqua: [0.3, 0.8, 0.8] as [number, number, number],
  indigo: [0.3, 0.3, 0.7] as [number, number, number],
  
  // Gradients (can be used for custom effects)
  sunset: [1.0, 0.4, 0.2] as [number, number, number],
  dawn: [1.0, 0.8, 0.6] as [number, number, number],
  midnight: [0.1, 0.15, 0.3] as [number, number, number],
} as const;

// Helper function to create custom colors
export const createFabricColor = (
  r: number,
  g: number,
  b: number
): [number, number, number] => {
  return [
    Math.max(0, Math.min(1, r)),
    Math.max(0, Math.min(1, g)),
    Math.max(0, Math.min(1, b)),
  ];
};

// Helper to convert hex color to fabric color format
export const hexToFabricColor = (hex: string): [number, number, number] => {
  // Remove # if present
  const cleanHex = hex.replace("#", "");
  
  // Parse hex values
  const r = parseInt(cleanHex.substring(0, 2), 16) / 255;
  const g = parseInt(cleanHex.substring(2, 4), 16) / 255;
  const b = parseInt(cleanHex.substring(4, 6), 16) / 255;
  
  return [r, g, b];
};

// Helper to adjust brightness of a color
export const adjustBrightness = (
  color: [number, number, number],
  factor: number
): [number, number, number] => {
  return [
    Math.max(0, Math.min(1, color[0] * factor)),
    Math.max(0, Math.min(1, color[1] * factor)),
    Math.max(0, Math.min(1, color[2] * factor)),
  ];
};
