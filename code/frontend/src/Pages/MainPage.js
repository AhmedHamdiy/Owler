import React, { useEffect,useNavigate,useState } from 'react';
import Owl from '../Styles/Owl.png'
import searchingOwl from '../Styles/searchingOwl.gif'
import SearchBar from "../Components/SearchBar.js";
function SearchPage() {
const [query,setQuery]=useState('');
const [searching,setSearching]=useState(false);
const hootQuery =async()=>{
    console.log('Hoot button clicked');
    setSearching(true);
};
// const navigate=useNavigate();
//     const [query,setQuery]=useState('');
//     const makeSuggestions =async(e)=>{
//         // setQuery(e.target.value);
//         //Connect the prevoius queries database to suggest suitable query
//     };
    
//     const hootQuery = async()=>{ 
//         // if (query==='') {
//         //     return;
//         // }   
//         // try {
//         //    //Precprocess the query??
    
//         //    //Search for it in the database then navigate to the query page result with query-id page?  
//         //     //If not Found in database navigate to NotFound Page
            
//         //     //navigate(`/ResultsPage/${queryResult[0].query_id}`);
//         //   } catch (error) {
//         //     console.error('Search error:', error.response ? error.response.data : error.message);
//         //     alert('failed hoot. Please check your query and try again.');
//         //   }
//         };
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
//  }), [];  

    return (
    <div>
  <div style={styles.mainContainer}>
    {
        searching ? 
        <iframe src={searchingOwl} width={648} height={648} frameBorder="0" allowFullScreen></iframe>     
        : <img src={Owl} alt="Owl" height={648} width={648}/> 
    }
    <SearchBar/>
    <div style={styles.buttonsContainer}>
    <input type="submit" value="Hoot" style={styles.button} onClick={hootQuery}/>
    <input type="submit" value="Random" style={styles.button} />
    </div>
  </div>
  </div>
  );
}
const styles = {
    mainContainer: {
        backgroundColor: "#2B2012",
        display: "flex",
        flexDirection: "column",
        alignItems: "center", 
        justifyContent:"start",
        height: "95vh",
        gap:"10px",
        paddingTop: "5vh"
    },
    buttonsContainer: {
        display: "flex",
        flexDirection: "row",
        justifyContent: "center",
        gap: "20px"
    },

    button: {
        borderRadius: "25px",
        width: "100px",
        margin: "0 auto",
        backgroundColor: "#E8CFC1",
        height: "50px",
        fontWeight: "bolder",
        fontSize: "larger",
        color: "#2B2012",
        cursor: "pointer",
    }
      
};

export default SearchPage;

