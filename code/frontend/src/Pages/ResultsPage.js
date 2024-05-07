import React, { useState, useEffect } from "react";
import Owl from '../Styles/Owl.png'
import SearchBar from "../Components/SearchBar.jsx";
import NotFound from "../Components/NotFound.jsx";
import SearchResults from "../Components/SearchResults.jsx";

function ResultsPage(props) {
    const [searchTime, setSearchTime] = useState(0);
    const [results, setResults] = useState([]);
    const queryParams = new URLSearchParams(props.location.search);
    const query = queryParams.get('q');

    useEffect(() => {
        const startTime = new Date().getUTCMilliseconds();
        const fetchData = async () => {
            try {
                // Replace your URL with the actual endpoint
                const response = await fetch(`http://localhost:5000/search/${query}`);
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                const data = await response.json();
                setResults(data);
            } catch (error) {
                console.error('Error fetching data:', error);
            }
            const endTime = new Date().getUTCMilliseconds();
            setSearchTime((endTime - startTime) / 1000);
        };
        
        // Call the fetch function
        fetchData();
    }, [query]);

    return (
        <div style={styles.resultsPageContainer}>
            <nav style={styles.nav}>
                <img src={Owl} alt="Owl" style={styles.owlImg} />
                <SearchBar />
            </nav>
            <h1 style={styles.heading3}>search time = {searchTime} seconds</h1>
            {results.length === 0 ? <NotFound /> : <SearchResults results={results} query={query} />}
        </div>
    );
}

const styles = {
    owlImg: {
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
    resultsPageContainer: {
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
    heading3: {
        color: '#E8CFC1',
    }
};

export default ResultsPage;
