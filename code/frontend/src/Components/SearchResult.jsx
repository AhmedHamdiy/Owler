import React from 'react';
import '../Styles/style.css'

function SearchResult ({title,link,snippet,query,icon}){
    const handleSnippet = (query, text) => {
        const queryWords = query.split(' ');
        let startIndex = text.indexOf(queryWords[0]);
        if (text.length > 100)
            if (startIndex !== 0)
                startIndex--;
            text=text.substring(startIndex,startIndex+100)+"...";
            return text;
    }
    return(
        <div className='result-container'>   
        <img src={icon} alt="icon" width={60} height={60}/>
        <div className='result-content-container'>
            <h1 className='result-title'>{title}</h1>
            <a className='result-link' href={link}>{link}</a>
            <p className='result-snippet'>{handleSnippet(query,snippet)}</p>
        </div>
    </div>
    );
};

export default SearchResult;