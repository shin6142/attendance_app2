import "../scss/list.scss"
import {TableRow} from "./TableRow.tsx";
import {DailyAttendances} from "../../types";
import {useState} from "react";
import {AxiosResponse} from "axios";
import {instance} from "../../query";
import {useQuery} from "react-query";

export const AttendancesTable = () => {
    const {dailyAttendances,date, updateYearMonth} = useAttendances()

    return (
        <div>
            <h1>{date.getFullYear()}/{date.getMonth() + 1}</h1>
            <button onClick={() => {
                updateYearMonth(date.getFullYear(), date.getMonth() - 1)
            }}>前月
            </button>
            <button onClick={() => {
                updateYearMonth(date.getFullYear(), date.getMonth() + 1)
            }}>次月
            </button>
            <div>
                <p>You are: {dailyAttendances?.employee_name}</p>
            </div>

            <table className="table" id={"attendances"}>
                <thead>
                <tr>
                    <th>勤務日</th>
                    <th>打刻種別</th>
                    <th>打刻時間</th>
                    <th>メッセージ</th>
                </tr>
                </thead>
                <tbody>
                {dailyAttendances?.attendances.map(dailyAttendance =>
                    dailyAttendance.attendances.map((attendance, i) =>
                        <TableRow
                            key={i}
                            date={dailyAttendance.date}
                            attendance={attendance}
                            parentState={dailyAttendances?.attendances ?? []}
                        ></TableRow>
                    )
                )}
                </tbody>
            </table>
        </div>
    )
}


const useAttendances = () => {
    const [date, setDate] = useState<Date>(new Date());

    const updateYearMonth = (year: number, month: number) => {
        setDate(new Date(year, month))
    }

    const fallback: DailyAttendances = {
        employee_id: 0,
        employee_name: "",
        attendances: []
    }

    const getDailyAttendances = async (employeeId: string, channelName: string, year: string, month: string): Promise<DailyAttendances> => {
        const {data} = await instance.get<DailyAttendances, AxiosResponse<DailyAttendances>>(`/attendances/${employeeId}/${channelName}/${year}/${month}`)
        return data
    }

    // for useQuery and prefetchQuery
    const commonOptions = {
        staleTime: 0,
        gcTime: 30000, // 5 minutes
    };

    const {data: dailyAttendances = fallback} = useQuery({
        queryKey: ["attendances", date.getFullYear(), date.getMonth() + 1],
        queryFn: () => getDailyAttendances("U02FFCC308G", "grp-dev-勤怠", date.getFullYear().toString(), (date.getMonth() + 1).toString()),
        ...commonOptions
    });

    return {dailyAttendances, date, updateYearMonth}
}