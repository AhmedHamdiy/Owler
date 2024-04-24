import React from "react";
import Owl from '../Styles/Owl.png'

function SearchResult ({title,link,snippet,query,icon}){
    const makeQueryBold = (query, text) => {
        const queryWords = query.split(' ');
        queryWords.forEach(word => {
            const regex = new RegExp(word, 'gi'); // Create a regular expression with 'gi' flags for global and case-insensitive search
            text = text.replace(regex, `<span style={{ color: "#2B2012", fontWeight: "bold" }}>${word}</span>`);
        });
        return <span dangerouslySetInnerHTML={{ __html: text }} style={styles.resultSpan}/>;
    }
    return(
        <div style={styles.resultContainer}>   
        <img src={icon} alt="icon" width={50} height={50}/>
    <div style={styles.resultContentContainer}>
        <h1 style={styles.resultTitle}>{title}</h1>
        <a style={styles.resultLink} href={link}>{link}</a>
        <p style={styles.resultSnippet}>{makeQueryBold(query,snippet)}</p>
    </div>
    </div>
    );
};

const styles = {
    resultContainer: {
        borderRadius: "25px",
        height: "150px",
        backgroundColor:"#E8CFC1",
        paddingTop: "10px",
        paddingLeft: "10px",
        width: "50%",
        display: "flex",
        flexDirection: "row",
        alignItems: "start",
        justifyContent: "start",
        gap: "10px",
    },
    resultContentContainer: {
        height: "100px",
        width: "100%",
        display: "flex",
        flexDirection: "column",
        alignItems: "start",
        justifyContent: "start",
    },
    resultTitle: {
        borderRadius: "25px",
        height: "52px",
        paddingRight: "50px",
        fontSize: "24px",
        color: "#2B2012",
        width: "100%"
    },
    resultLink: {
        borderRadius: "25px",
        height: "52px",
        paddingRight: "50px",
        fontSize: "smaller",
        width: "100%"
    },
    resultSpan:{
        width: "90%",
        fontSize: "14px",
        overflow: 'hidden',
        whiteSpace: 'wrap',
        textOverflow: 'ellipsis',
    }
};
export default SearchResult;