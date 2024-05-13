import React from 'react';
import '../Styles/style.css';
import defaultIcon from '../Styles/img/Owl.png';
function SearchResult({ title, link, snippet, icon }) {
    const iconSrc = (icon && icon!="code/frontend/src/Styles/def-logo.svg") ? icon : defaultIcon;

    return (
        <div className='result-container'>
            <img src={iconSrc} alt="icon" className='result-icon'/>
            <div className='result-content-container'>
                <h1 className='result-title'>{title}</h1>
                <a className='result-link' href={link}
                >{link}</a>
                <p className='result-snippet' dangerouslySetInnerHTML={{ __html: snippet }} />
            </div>
        </div>
    );
}

export default SearchResult;
