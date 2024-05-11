import React, { useState } from "react";
import SearchResult from "./SearchResult.jsx";
import '../Styles/style.css'

function SearchResults ({results,query})  {
    const MAX_RESULTS_PER_PAGE=10;
    const [currentPage,setCurrentPage]=useState(0);
    return (
        <div className="results-container">
        {results.slice(currentPage * MAX_RESULTS_PER_PAGE, (currentPage + 1) * MAX_RESULTS_PER_PAGE)
            .map((result, index) => (<SearchResult key={index} 
                title={result.Title} link={result.URL} snippet={result.Snippet} 
                query={query} icon={result.Logo}/>
            ))}
        <div className="pagination">
            <button className="previous-page" onClick={() => setCurrentPage(currentPage - 1)} disabled={currentPage === 0}>Previous</button>
            <span className="current-page">{currentPage + 1}</span>
            <button className="next-page" onClick={() => setCurrentPage(currentPage + 1)} disabled={(currentPage + 1) * MAX_RESULTS_PER_PAGE >= results.length}>Next</button>
        </div>
        </div>
    );
}

export default SearchResults;