import "./scss/list.scss"
import {useEffect, useState} from "react";
import {TableRow} from "./TableRow.tsx";
import {Attendances, DailyAttendance} from "../types";
import {authenticate, fetchAttendances, getFreeeLoginUser, getTokenFromQueryParameter} from "../query";
import {RegisterAttendancesButton} from "./RegisterAttendancesButton.tsx";
import {useForm} from "react-hook-form"


export const Table = () => {
    const [employeeId, setEmployeeId] = useState("U02FFCC308G")
    const [freeeEmployeeId, setFreeeEmployeeId] = useState("")
    const [year, setYear] = useState(new Date().getFullYear().toString())
    const [month, setMonth] = useState((new Date().getMonth() + 1).toString())
    const [attendances, setAttendances] = useState<Attendances>()
    const [postData, setPostData] = useState<DailyAttendance[]>([]);
    const [displayRows, setDisplayRows] = useState<DailyAttendance[]>([]);
    const updateAttendances = () => {
        fetchAttendances(employeeId, year, month).then(result => {
                setAttendances(result)
            }
        )
    }

    useEffect(() => {
        setPostData(attendances?.attendances || [])
    }, [attendances]);

    useEffect(() => {
        fetchAttendances(employeeId, year, month).then(result => {
                setAttendances(result)
                setPostData(result.attendances)
                setDisplayRows(rows(parseInt(year, 10), parseInt(month, 10), result.attendances || []))
            }
        )
        getFreeeLoginUser(getTokenFromQueryParameter() ?? "").then(result => {
            setFreeeEmployeeId(result.id.toString())
        })
    }, []);

    const getWeekdaysOfMonth = (year: number, month: number): Date[] => {
        const weekdays: Date[] = [];
        // 月の最初の日
        let currentDate = new Date(year, month - 1, 1);

        // 月の最終日まで繰り返す
        while (currentDate.getMonth() === month - 1) {
            // 日曜日（0）または土曜日（6）でない場合、平日としてリストに追加
            if (currentDate.getDay() !== 0 && currentDate.getDay() !== 6) {
                weekdays.push(new Date(currentDate));
            }

            // 次の日に進む
            currentDate.setDate(currentDate.getDate() + 1);
        }

        return weekdays;
    }


    const formatDateTime = (date: Date): string => {
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        const seconds = date.getSeconds().toString().padStart(2, '0');

        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }

    const formatDate = (date: Date): string => {
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    const rows = (year: number, month: number, dailyAttendances: DailyAttendance[]): DailyAttendance[] => {
        const weekdays = getWeekdaysOfMonth(year, month)
        const rows: DailyAttendance[] = []
        console.log(dailyAttendances)
        weekdays.forEach(weekday => {
            const dailyAttendance: DailyAttendance = {
                date: formatDate(weekday),
                attendances: []
            }
            dailyAttendances.forEach(dailyAttendance => {
                dailyAttendance.attendances.forEach(attendance => {
                    if (attendance.datetime.includes(formatDate(weekday))) {
                        dailyAttendance.attendances.push(attendance)
                    }
                })
            })
            console.log(dailyAttendance)
            rows.push(dailyAttendance)
        })
        return rows
    }

    const {register, handleSubmit} = useForm<Inputs>();
    type Inputs = {
        employeeId: string,
        year: string,
        month: string
    }

    return (
        <div>
            <form onSubmit={handleSubmit((inputs) => {
                fetchAttendances(inputs.employeeId, inputs.year, inputs.month).then((result) => {
                        setAttendances(result)
                        setPostData(result.attendances)
                    }
                )
            })}>
                <input {...register("employeeId")}/>
                <input {...register("year")}/>
                <input {...register("month")}/>
                <button type={"submit"}>更新</button>
            </form>
            <button onClick={authenticate}>認証</button>
            <input type={"text"} value={employeeId} onChange={(event) => {
                setEmployeeId(event.target.value)
            }}/>
            <input type={"number"} value={year} onChange={(event) => {
                setYear(event.target.value)
            }}/>
            <input type={"number"} value={month} onChange={(event) => {
                setMonth(event.target.value)
            }}/>
            <button onClick={updateAttendances}>更新</button>
            <RegisterAttendancesButton postData={postData} year={year} month={month} employeeId={freeeEmployeeId}
                                       token={getTokenFromQueryParameter()}></RegisterAttendancesButton>
            <table className="table" id={"attendances_table"}>
                <thead>
                <tr>
                    <th>従業員ID</th>
                    <th>従業員名</th>
                    <th>勤務日</th>
                    <th>打刻種別</th>
                    <th>打刻時間</th>
                    <th>メッセージ</th>
                </tr>
                </thead>
                <tbody>
                {attendances?.attendances.map(dailyAttendance =>
                    dailyAttendance.attendances.map((attendance, i) =>
                        <TableRow key={i}
                                  date={dailyAttendance.date}
                                  attendance={attendance}
                                  setState={(arg) => {
                                      console.log(arg)
                                  }}
                                  parentState={attendances?.attendances ?? []}
                        ></TableRow>
                    )
                )}
                {/*{rows(parseInt(year), parseInt(month), attendances?.attendances ?? [])?.map(dailyAttendance =>*/}
                {/*    dailyAttendance.attendances.map((attendance, i) =>*/}
                {/*        <TableRow key={i}*/}
                {/*                  date={dailyAttendance.date}*/}
                {/*                  attendance={attendance}*/}
                {/*                  setState={(arg) => {*/}
                {/*                      console.log(arg)*/}
                {/*                  }}*/}
                {/*                  parentState={attendances?.attendances ?? []}*/}
                {/*        ></TableRow>*/}
                {/*    )*/}
                {/*)}*/}
                </tbody>
            </table>
        </div>
    )
}
