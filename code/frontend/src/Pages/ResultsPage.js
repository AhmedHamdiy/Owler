import React, { useState ,useEffect} from "react";
import { useNavigate } from 'react-router-dom';
import Owl from '../Styles/Owl.png'
import SearchResult from "../Components/SearchResult.js";
import SearchBar from "../Components/SearchBar.js";
function ResultsPage ()  {
    const MAX_RESULTS_PER_PAGE=5;
    const [totalResults,setTotalResults]=useState(0);
    const [currentPage,setCurrentPage]=useState(0);
    const [query,setQuery]=useState(''); 
    const [results, setResults] = useState([
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon: "https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon: "https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon: "https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon: "https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon: "https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
        {
            title: 'Crowler',
            icon:"https://github.com/fluidicon.png",
            query: 'java',
            link: 'https://github.com/AhmedHamdiy/Crowler',
            snippet: 'Crowler: A Multithreaded Crawler-Based Search Engine. It is developed in Java for an Advanced Programming Techniques (APT) course.',
        },
        {
            title: 'Sanay3y On The Go ',
            icon:"https://github.com/fluidicon.png",
            query: 'users',
            link: 'https://github.com/jpassica/Sanay3yOnTheGo',
            snippet: 'Welcome to our service network project! This application allows users to order technician services seamlessly.',
        },
    ]);
    const [searchTime, setSearchTime] = useState('');
    // const navigate=useNavigate();
    //Fetch results from the database and set the Search Time
    // useEffect(() => {
    //     // const searchButton = document.querySelector('.hoot');
    //     // const randomButton = document.querySelector('.random');
    //     // const queryTextBox = document.querySelector('.query-text-box');
    //     // searchButton.addEventListener('click', () => {
    //     //     console.log('Hoot button clicked');
    //     //     console.log('Query:', queryTextBox.value);
    //     // });
    //     // randomButton.addEventListener('click', () => {
    //     //     console.log('Random button clicked');
    //     // });
    //     // const queryTextBox = document.querySelector('.query-text-box');
    //     // console.log('Query:', queryTextBox.value);
    //     // setQuery(queryTextBox.value);
    //     // const suggestions = await axios.get(`http://localhost:3000/suggestions/${queryTextBox.value}`);
    //     // console.log('Suggestions:', suggestions.data);
    //     // const suggestionList = document.querySelector('.suggestion-list');
    //     // suggestionList.innerHTML = '';
    //     // suggestions.data.forEach((suggestion) => {
    //     //     const suggestionElement = document.createElement('li');
    //     //     suggestionElement.textContent = suggestion;
    //     //     suggestionList.appendChild(suggestionElement);
    //     // });
    //     // suggestionList.style.display = 'block';
    //     // return () => {
    //     //     suggestionList.style.display = 'none';
    //     // };
    // }, []);

    return (
        <div style={styles.resultsPageContainer}>
            <nav style={styles.nav}>
            <img src={Owl} alt="Owl" width={50} height={50} padding={10}/>     
            <SearchBar/>
            </nav>
            {results.slice(currentPage * MAX_RESULTS_PER_PAGE, (currentPage + 1) * MAX_RESULTS_PER_PAGE).map((result, index) => (
            <SearchResult key={index} title={result.title} link={result.link} snippet={result.snippet} query={result.query} icon={result.icon}/>
            ))}
            <div style={styles.pagination}>
                <button style={styles.previousPage} onClick={() => setCurrentPage(currentPage - 1)} disabled={currentPage === 0}>Previous</button>
                <span style={styles.currentPage}>{currentPage + 1}</span>
                <button style={styles.nextPage} onClick={() => setCurrentPage(currentPage + 1)} disabled={(currentPage + 1) * MAX_RESULTS_PER_PAGE >= results.length}>Next</button>
            </div>
        </div>
    );

}

const styles={
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
    resultsPageContainer:{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'start',
        gap: '20px',
        backgroundColor: '#2B2012',
        color: 'white',
        height: '96vh',
        padding: '20px',    
    },
    pagination: {
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'center',
        gap: '20px',
    },
    previousPage: {
        cursor: 'pointer',
        backgroundColor: '#E8CFC1',
        color: '#2B2012',
        borderRadius: '10px',
        padding: '10px',
    },
    nextPage: {
        cursor: 'pointer',
        backgroundColor: '#E8CFC1',
        color: '#2B2012',
        borderRadius: '10px',
        padding: '10px',
    },
    currentPage: {
        fontSize: '24px',
    }

};

export default ResultsPage;