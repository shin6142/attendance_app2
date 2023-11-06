import {useEffect, useState} from "react";
import TextField from "@material-ui/core/TextField";
import {Attendance, DailyAttendance} from "../types";

export const TableRow = (props: TableRowProps) => {
    const [datetime, setDatetime] = useState<string>(props.attendance.datetime)
    useEffect(() => {
        setDatetime(props.attendance.datetime)
    }, [props.parentState])

    return (
        <tr>
            <td>{props.attendance.employee_id}</td>
            <td>{props.attendance.employee_name}</td>
            <td>{props.date}</td>
            <td>{props.attendance.kind}</td>
            <td>
                <TextField value={datetime} onChange={(event) => {
                    setDatetime(event.target.value)
                    const updated = props.parentState.map((dailyAttendance) => {
                        if (dailyAttendance.date == props.date) {
                            dailyAttendance.attendances.map((attendance) => {
                                if (attendance.kind == props.attendance.kind) {
                                    attendance.datetime = datetime
                                }
                            })
                        }
                        return dailyAttendance
                    })
                    props.setState(updated)
                }}/>
            </td>
            <td>{props.attendance.context}</td>
        </tr>
    );
}

type TableRowProps = {
    date: string
    attendance: Attendance
    parentState: DailyAttendance[]
    setState: (dailyAttendances: DailyAttendance[]) => void
}