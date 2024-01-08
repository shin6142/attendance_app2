import './App.css'
import {QueryClient, QueryClientProvider} from "react-query";
import {ReactQueryDevtools} from "react-query/devtools";
import {Router} from "./Router.tsx";

const queryClient = new QueryClient()
const App = () => {

    return (
        <div className="App">
            <QueryClientProvider client={queryClient}>
                <Router />
                <ReactQueryDevtools initialIsOpen={false}/>
            </QueryClientProvider>
        </div>
    );
}
export default App;
