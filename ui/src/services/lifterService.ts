import { api } from 'boot/axios';
import type { ApiResponse } from './response';

export interface PersonalBest {
  name: string;
  sex: string;
  equipment: string;
  best3SquatKg?: number;
  best3BenchKg?: number;
  best3DeadliftKg?: number;
  totalKg?: number;
  dots?: number;
  wilks?: number;
  glossbrenner?: number;
  goodlift?: number;
}

export interface CompetitionResult {
  place: string;
  federation: string;
  date: Date;
  meetCountry: string;
  meetName: string;
  division: string;
  age: number;
  equipment: string;
  weightClassKg: string;
  bodyweightKg: number;
  squat1Kg?: number;
  squat2Kg?: number;
  squat3Kg?: number;
  squat4Kg?: number;
  best3SquatKg?: number;
  bench1Kg?: number;
  bench2Kg?: number;
  bench3Kg?: number;
  bench4Kg?: number;
  best3BenchKg?: number;
  deadlift1Kg?: number;
  deadlift2Kg?: number;
  deadlift3Kg?: number;
  deadlift4Kg?: number;
  best3DeadliftKg?: number;
  totalKg?: number;
  dots?: number;
  wilks?: number;
  glossbrenner?: number;
  goodlift?: number;
}

export async function findLifters(namePattern: string): Promise<ApiResponse<string[]>> {
  const response = await api.get<ApiResponse<string[]>>(`/lifter`, {
    params: {
      namePattern: namePattern
    }
  });
  return response.data;
}

export async function findPersonalBests(name: string): Promise<ApiResponse<PersonalBest[]>> {
  const response = await api.get<ApiResponse<PersonalBest[]>>(`/lifter/${name}/personal-bests`);
  return response.data;
}

export async function findCompetitionResults(name: string): Promise<ApiResponse<CompetitionResult[]>> {
  const response = await api.get<ApiResponse<CompetitionResult[]>>(`/lifter/${name}/competition-results`);
  return response.data;
}
