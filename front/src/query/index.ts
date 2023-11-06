import {Attendances, DailyAttendance} from "../types";
import axios, {AxiosResponse} from "axios";

export const downloadAttendancesCsv = (useId: String, year: String, month: String) => {
    window.location.href = `http://localhost:8080/attendances/${useId}/${year}/${month}/download`;
};

export const downloadMessageHistory = (useId: String, year: String, month: String) => {
    window.location.href = `http://localhost:8080/message/${useId}/${year}/${month}/download`;
};

const instance = axios.create({
    baseURL: 'http://localhost:8080'
})

export const fetchAttendances = async (employeeId: string, year: string, month: string): Promise<Attendances> => {
    const {data} = await instance.get<Attendances, AxiosResponse<Attendances>>(`/attendances/${employeeId}/${year}/${month}`)
    return data
}

export const postAttendances = async (attendances: DailyAttendance[], employeeId: string, year: string, month: string): Promise<[]> => {
    const {data} = await instance.post<[], AxiosResponse<[]>>(`/attendances/${employeeId}/${year}/${month}/record`, attendances)
    return data
}