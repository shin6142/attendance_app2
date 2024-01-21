import {authenticate} from "../../query";

export const FreeeAuthenticationButton = () => {
    return (
        <button onClick={() => {
            authenticate()
        }}>Freeeログイン</button>
    )
}