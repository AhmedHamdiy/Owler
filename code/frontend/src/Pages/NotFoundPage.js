import React,{param} from "react";
import SearchBar from "../Components/SearchBar";
import Owl from '../Styles/Owl.png'
function NotFound(){
    // const NotFoundQuery = param.qid;

    const NotFoundQuery = 'Hoot';
    return(
        <div style={styles.notFoundPage}>
             <nav style={styles.nav}>
            <img src={Owl} alt="Owl" width={50} height={50}/>     
            <SearchBar query={NotFoundQuery}/>
            </nav>
             <div style={styles.notFoundSuggestions}>
            <h1>404 Not Found</h1>
            No results containing all your search terms were found.<br/>
            Your search query <i>{NotFoundQuery}</i> did not match any documents.
            
            <h2>Suggestions:</h2>
            <ul>
                <li>Make sure that all words are spelled correctly.</li>
                <li>Try different keywords.</li>
                <li>Try more general keywords.</li>
            </ul>
            </div>
             </div>
    );
}


const styles = {
    nav: {
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'flex-start',
        backgroundColor: '#2B2012',
        color: 'white',
        gap: '20px',
        padding: '10px',
        fontSize: 'larger',
        fontWeight: 'bolder',
        height: '5vh',
      },
    notFoundSuggestions: {
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
    notFoundPage: {
        height: '100vh',
        backgroundColor: '#2B2012',
        color: 'white',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'space-around',
    }
};

export default NotFound;
