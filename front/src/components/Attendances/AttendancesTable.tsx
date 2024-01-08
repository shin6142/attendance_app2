import "../scss/list.scss"
import {TableRow} from "./TableRow.tsx";
import {useAttendances} from "./hooks/useAttendances.ts";
// import {useEffect, useState} from "react";
export const AttendancesTable = () => {
    // const [freeeEmployeeId, setFreeeEmployeeId] = useState("")
    // const [postData, setPostData] = useState<DailyAttendance[]>([]);

    const {dailyAttendances,date, updateYearMonth} = useAttendances()

    // useEffect(() => {
    //     setPostData(dailyAttendances?.attendances || [])
    // }, [dailyAttendances]);

    // useEffect(() => {
    //     getFreeeLoginUser(getTokenFromQueryParameter() ?? "").then(result => {
    //         setFreeeEmployeeId(result.id.toString())
    //     })
    // }, []);

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
            {/*<button onClick={authenticate}>認証</button>*/}
            {/*<RegisterAttendancesButton postData={postData} year={yearMonth.year.toString()} month={yearMonth.month.toString()} employeeId={freeeEmployeeId}*/}
            {/*                           token={getTokenFromQueryParameter()}></RegisterAttendancesButton>*/}
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
                            kind={attendance.kind}
                            key={i}
                            date={dailyAttendance.date}
                            attendance={attendance}
                            setState={() => {}}
                            parentState={dailyAttendances?.attendances ?? []}
                        ></TableRow>
                    )
                )}
                </tbody>
            </table>
        </div>
    )
}