import React from "react";
import '../Styles/style.css'
function NotFound() {
    return(
        <div className='not-found'>
            <h1>404 Not Found</h1>
            <p>
            No results containing all your search terms were found.<br/>
            </p>
            <h2>Suggestions:</h2>
            <ul>
                <li>Make sure that all words are spelled correctly.</li>
                <li>Try different keywords.</li>
                <li>Try more general keywords.</li>
            </ul>
        </div>
    );
}

export default NotFound;