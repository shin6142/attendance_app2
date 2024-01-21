import {AttendancesTable as Table } from "../../components/Attendances/AttendancesTable.tsx";
import {FreeeAuthenticationButton} from "../../components/Attendances/FreeeAuthenticationButton.tsx";

export const AttendancesTable = () => {
    return (
        <>
            <FreeeAuthenticationButton />
            <Table />
        </>
    );
};
