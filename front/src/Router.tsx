import {createBrowserRouter, RouterProvider} from "react-router-dom";
import {AttendancesTable} from "./Pages/Attendances/AttendancesTable.tsx";
import {FC} from "react";

export const PATH = {
    ATTENDANCES: "/attendances",
}

export const router = createBrowserRouter(
    [
        {
            path: "/",
            children: [
                {
                    path: PATH.ATTENDANCES,
                    element: <AttendancesTable/>,
                },
            ],
        }
    ],
    { basename: "/" }
)

export const Router: FC = () => {
    return <RouterProvider router={router} />;
};
