import React, { useState ,useEffect} from "react";
import Owl from '../Styles/Owl.png'
import SearchBar from "../Components/SearchBar.jsx";
import NotFound from "../Components/NotFound.jsx";
import SearchResults from "../Components/SearchResults.jsx";
// import axios from 'axios';
function ResultsPage (props)  {
    const [searchTime,setSearchTime]=useState(0);
    const [results,setResults]=useState([]);
    const queryParams = new URLSearchParams(props.location.search);
    const query = queryParams.get('q');
    const getResults = async () => {
        //const response = await axios.post(`http://localhost:5000/search/${query}`);
        const response = await axios.post('http://localhost:5000/search', query, {
            headers: {
                'Content-Type': 'text/plain', // Specify the content type as text/plain for raw data
            },
        });
        console.log('Results:', response.data);
        setResults(response.data);
    };
    useEffect(() => {
        const startTime=new Date().getUTCMilliseconds();
        alert(query);
        getResults();
        /* //if(query==='Multithreaded'){
            setResults([
                {
                    title: 'Crowler',
                    icon:"https://github.com/fluidicon.png",
                    link: 'https://github.com/AhmedHamdiy/Crowler',
                    snippet: 'Lorem ipsum dolor sit amet consectetur adipisicing elit. Dolorum vero adipisci ab est expedita, aspernatur accusantium quam ratione repudiandae blanditiis ducimus quis natus. Quasi sed omnis natus! Quaerat, obcaecati quis! Crowler: A Multithreaded Crawler Multithreaded ipsum dolor sit amet Multithreaded . Dolorum vero adipisci ab est expedita, aspernatur accusantium quam ratione repudiandae blanditiis ducimus quis natus. Quasi sed omnis natus! Quaerat, obcaecati quis!',
                },
                {
                    title: 'Sanay3y On The Go ',
                    icon:"https://github.com/fluidicon.png",
                    link: 'https://github.com/jpassica/Sanay3yOnTheGo',
                    snippet: 'Welcome to our network project! This Multithreaded allows users This application This application This application This application This application This application This application  to order technician services seamlessly.',
                },
            ]);
        } */
        const endTime=new Date().getUTCMilliseconds();
        setSearchTime((endTime-startTime)/1000);
    }, []);

    return (
        <div style={styles.resultsPageContainer}>
            <nav style={styles.nav}>
                <img src={Owl} alt="Owl" style={styles.owlImg}/>     
                <SearchBar/>
            </nav>
            <h1 style={styles.heading3}>search time = {searchTime} seconds</h1>
            {results.length === 0 ? <NotFound/> : <SearchResults results={results} query={query}/>}
        </div>
    );

}
const styles={
    owlImg:{
        height: '80px',
        width: '80px',
        marginTop: '10px',
    },
    nav: {
        position: 'sticky', 
        top: 0, 
        zIndex: 100,
        backgroundColor: '#2B2012',
        color: 'white',
        gap: '20px',
        padding: '10px',
        fontSize: 'larger',
        fontWeight: 'bolder',
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        width: '100%',
    },
    resultsPageContainer:{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'start',
        gap: '20px',
        backgroundColor: '#2B2012',
        color: 'white',
        padding: '0 20px',    
        height: '220vh',
    },
    pagination: {
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'center',
        gap: '20px',
    },
    previousPage: {
        cursor: 'pointer',
        backgroundColor: '#E8CFC1',
        color: '#2B2012',
        borderRadius: '10px',
        padding: '10px',
        width: '100px',
        fontSize: '20px',
    },
    nextPage: {
        cursor: 'pointer',
        backgroundColor: '#E8CFC1',
        color: '#2B2012',
        borderRadius: '10px',
        padding: '10px',
        width: '100px',
        fontSize: '20px',
    },
    currentPage: {
        padding: '10px',
        fontSize: '24px',
    },
    heading3:{
        color: '#E8CFC1',
    }
};
export default ResultsPage;