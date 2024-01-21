import React, {useEffect, useState} from "react";
import {getFreeeLoginUser, postAttendances} from "../../query";
import {DailyAttendance} from "../../types";

export const RegisterAttendancesButton = (props: RegisterAttendancesButtonProps) => {
    const [value, setValue] = useState("Freeeに勤怠を登録する")
    const [disable, setDisable] = React.useState(false);
    const [employeeId, setEmployeeId] = useState(0)

    useEffect(() => {
        if (props.token !== null){
            getFreeeLoginUser(props.token).then((data) => {
                setEmployeeId(data.id)
            })
        }
    }, [props.token]);
    return (
        <button disabled={disable} onClick={() => {
            setValue("登録中です")
            setDisable(true)
            postAttendances(props.postData, employeeId.toString(), props.year, props.month, props.token).finally(() => {
                setDisable(false)
                setValue("Freeeに勤怠を登録する")
            })
        }}>{value}</button>
    )
}

type RegisterAttendancesButtonProps = {
    postData: DailyAttendance[]
    year: string
    month: string
    token: string | null
}