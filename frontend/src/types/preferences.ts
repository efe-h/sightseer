export interface Preferences {
  history: number;
  art: number;
  architecture: number;
  nature: number;
  science: number;
  food: number;
  entertainment: number;
  shopping: number;
  views: number;
  family: number;
}

export type PreferenceName = keyof Preferences;