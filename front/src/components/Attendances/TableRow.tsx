import {useEffect, useState} from "react";
import TextField from "@material-ui/core/TextField";
import {Attendance, DailyAttendance} from "../../types";

export const TableRow = (props: TableRowProps) => {
    const {
        kind,
        setKind,
        datetime,
        setDatetime
    } = useAttendance(props)

    return (
        <tr>
            <td>{props.date}</td>
            <td>
                <TextField className={props.attendance.kind} value={kind} onChange={(event) => {
                    setKind(event.target.value)
                }}/>
            </td>
            <td>
                <TextField value={datetime} onChange={(event) => {
                    setDatetime(event.target.value)
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
}

const useAttendance = (props: TableRowProps) => {
    const [kind, setKind] = useState<string>(props.attendance.kind)
    const [datetime, setDatetime] = useState<string>(props.attendance.datetime)
    useEffect(() => {
        setKind(props.attendance.kind)
        setDatetime(props.attendance.datetime)
    }, [props.parentState])

    useEffect(() => {
        props.parentState.map((dailyAttendance) => {
            if (dailyAttendance.date == props.date) {
                dailyAttendance.attendances.map((attendance) => {
                    if (attendance.attendance_id == props.attendance.attendance_id) {
                        attendance.datetime = datetime
                    }
                })
            }
        })
        console.log(props.parentState)
    }, [kind, datetime]);

    return {
        kind,
        setKind,
        datetime,
        setDatetime
    }
}