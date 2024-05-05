import React from "react";
function NotFound(){
    return(
        <div style={styles.notFound}>
            <h1>404 Not Found</h1>
            <p>
            No results containing all your search terms were found.<br/>
            {/* Your search query <i><b>{query}</b></i> did not match any documents. */}
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


const styles = {
    notFound: {
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
    }
};

export default NotFound;