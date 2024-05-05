import React from 'react';
function SearchResult ({title,link,snippet,query,icon}){
    const handleSnippet = (query, text) => {
        const queryWords = query.split(' ');
        const textWords = text.split(' ');
        const startIndex = text.indexOf(queryWords[0]);
        let finalSnippet = "";
        textWords.forEach(word => {
            if(queryWords.includes(word))
                    finalSnippet += `<b><i>`+word +" "+ "</i></b>";
            else
                finalSnippet += word + " ";
        });
        if(finalSnippet.length>200)
            finalSnippet=finalSnippet.substring(startIndex,startIndex+200)+"...";
        return <span dangerouslySetInnerHTML={{ __html: finalSnippet }} style={styles.resultSpan}/>;
    }
    return(
        <div style={styles.resultContainer}>   
        <img src={icon} alt="icon" width={60} height={60}/>
    <div style={styles.resultContentContainer}>
        <h1 style={styles.resultTitle}>{title}</h1>
        <a style={styles.resultLink} href={link}>{link}</a>
        <p style={styles.resultSnippet}>{handleSnippet(query,snippet)}</p>
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
        maxWidth: "60%",
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
        whiteSpace: 'no-wrap',
        textOverflow: 'ellipsis',
    }
};
export default SearchResult;