import React, { useState, useEffect } from "react";
import Owl from '../Styles/Owl.png'
import '../Styles/style.css'
import SearchBar from "../Components/SearchBar.jsx";
import NotFound from "../Components/NotFound.jsx";
import SearchResults from "../Components/SearchResults.jsx";

function ResultsPage(props) {
    const [searchTime, setSearchTime] = useState(0);
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(true); // Add loading state
    const queryParams = new URLSearchParams(props.location.search);
    const query = queryParams.get('q');

    useEffect(() => {
        const startTime = performance.now();
        const fetchData = async () => {
            try {
                const requestOptions = {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'text/plain'
                    },
                    body: query
                };
    
                const response = await fetch("http://localhost:5000/search", requestOptions);
                
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                const data = await response.json();
                setResults(data);
            } catch (error) {
                console.error('Error fetching data:', error);
            } finally {
                const endTime = performance.now();
                setSearchTime(((endTime - startTime)/1000).toFixed(2));
                setLoading(false);
            }
        };
        
        fetchData();
    }, [query]);

    return (
        <div className="results-page-container ">
            <nav>
                <img src={Owl} alt="Owl" className="owl-img"/>
                <h5>search time = {searchTime} seconds</h5>
            </nav>
                <SearchBar onSuggest={() => { }}/>
            {loading ? ( // Render loading indicator if loading state is true
                <div style={{color:"#2B2012"}}>Loading...</div>
            ) : (
                results.length === 0 ? <NotFound /> : <SearchResults results={results} query={query} />
            )}
        </div>
    );
}

export default ResultsPage;
