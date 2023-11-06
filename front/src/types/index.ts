export type Attendances = {
    attendances: DailyAttendance[]
}

export type DailyAttendance = {
    date: string,
    attendances: Attendance[]
}

export type Attendance = {
    employee_id: number,
    employee_name: string,
    datetime: string,
    context: string,
    kind: string,
}