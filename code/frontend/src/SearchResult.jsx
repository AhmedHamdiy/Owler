import React from "react";


const SearchResult =({title,URL,snippet})=>{

    return(
    <div className="search-result">
        <h1 className="result-title">{title}</h1>
        <a className="result-url" href={URL}>{URL}</a>
        <p className="result-snippet">{snippet}</p>
    </div>
    );
};

export default SearchResult;