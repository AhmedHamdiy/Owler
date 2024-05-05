import React, { Fragment, useState } from "react";
import SearchResult from "./SearchResult.jsx";
function SearchResults ({results,query})  {
    const MAX_RESULTS_PER_PAGE=10;
    const [currentPage,setCurrentPage]=useState(0);
    return (
        <div style={styles.resultsContainer}>
        {results.slice(currentPage * MAX_RESULTS_PER_PAGE, (currentPage + 1) * MAX_RESULTS_PER_PAGE)
            .map((result, index) => (<SearchResult key={index} 
                title={result.title} link={result.link} snippet={result.snippet} 
                query={query} icon={result.icon}/>
            ))}
        <div style={styles.pagination}>
            <button style={styles.previousPage} onClick={() => setCurrentPage(currentPage - 1)} disabled={currentPage === 0}>Previous</button>
            <span style={styles.currentPage}>{currentPage + 1}</span>
            <button style={styles.nextPage} onClick={() => setCurrentPage(currentPage + 1)} disabled={(currentPage + 1) * MAX_RESULTS_PER_PAGE >= results.length}>Next</button>
        </div>
        </div>
    );
}

const styles={
    resultsContainer:{
        width: "80%",
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'start',
        gap: '20px',
        borderRadius: '20px',
    },
    page: {
        fontSize: '28px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'start',
        gap: '20px',
        backgroundColor: '#E8CFC1',
        borderRadius: '20px',
        padding: '20px',
        color: '#2B2012',
        width: '60%'
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
        fontSize: '20px',
        cursor: 'pointer',
        backgroundColor: '#E8CFC1',
        color: '#2B2012',
        borderRadius: '10px',
        padding: '10px',
        width: '100px',
    },
    currentPage: {
        fontSize: '24px',
        padding: '10px',
    }

};

export default SearchResults;