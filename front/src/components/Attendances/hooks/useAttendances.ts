import {DailyAttendances} from "../../../types";
import {useState} from "react";
import {AxiosResponse} from "axios";
import {instance} from "../../../query";
import {useQuery} from "react-query";

// for useQuery and prefetchQuery
const commonOptions = {
    staleTime: 0,
    gcTime: 30000, // 5 minutes
};

const getDailyAttendances = async (employeeId: string, channelName: string, year: string, month: string): Promise<DailyAttendances> => {
    const {data} = await instance.get<DailyAttendances, AxiosResponse<DailyAttendances>>(`/attendances/${employeeId}/${channelName}/${year}/${month}`)
    return data
}

export const useAttendances = () => {
    const [date, setDate] = useState<Date>(new Date());

    const updateYearMonth = (year: number, month: number) => {
        setDate(new Date(year, month))
    }

    const fallback: DailyAttendances = {
        employee_id: 0,
        employee_name: "",
        attendances: []
    }
    const {data: dailyAttendances = fallback} = useQuery({
        queryKey: ["attendances", date.getFullYear(), date.getMonth() + 1],
        queryFn: () => getDailyAttendances("U02FFCC308G", "grp-dev-勤怠", date.getFullYear().toString(), (date.getMonth() + 1).toString()),
        ...commonOptions
    });

    return {dailyAttendances, date, updateYearMonth}
}