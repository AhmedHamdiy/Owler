import React from 'react';
import '../Styles/style.css';

function SearchResult({ title, link, snippet, icon }) {
    return (
        <div className='result-container'>
            <img src={icon} alt="icon" width={60} height={60} />
            <div className='result-content-container'>
                <h1 className='result-title'>{title}</h1>
                <a className='result-link' href={link}>{link}</a>
                <p className='result-snippet' dangerouslySetInnerHTML={{ __html: snippet }} />
            </div>
        </div>
    );
}

export default SearchResult;
